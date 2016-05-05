package io.zeymo.map;

import io.zeymo.commons.io.impl.NeuronByteArrayReader;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created By Zeymo at 14-8-20 17:16 BinaryArray-Based-GC-Free-FixSize-HashMap 数据结构类似ConcurrentHashMap
 * 
 * <pre>
 *  <ul>
 *    <p>Segment</p>
 *    <graph>
 *                     pinOffset     freeOffset
 *                             |     |
 *     +---——--+-------+-------+     +-------+ __ +-------+ __ +-------+
 *     |k|p|n|v|k|p|n|v|k|p|n|v| --> |k|p|n|v| __ |k|p|n|v| __ |k|p|n|v|
 *     +----—--+-------+-------+     +-------+    +-------+    +-------+
 *      \      /
 *     entryOffset
 * 
 *    </graph>
 *    <li>initCapacity表示作为bucketEntry个数(pinOffset)</liE>
 *    <li>maxCapacity表示作entry的总个数</li>
 *    <li>keyLength表示key的有效长度</li>
 *    <li>valueLength表示value的有效长度</li>
 *    <li>
 *        除了initCapcity外的所有entry为doubleLinkedFreeEntry,
 *        freeOffset指向第一个freeEntryOffset,
 *        被分配之后顺序指向下一个entry,直到分配完(NIL)
 *    </li>
 *    <li>
 *        一个entry由key(?)+prev(4byte)+next(4byte)+value(?)顺序构成,
 *        为了保证充分利用cpu cacheline建议entry的bytes不要超过32或64，
 *        如果cacheline falsesharing问题比较严重请添加padding bytes
 *    <li/>
 *    <li>
 *        写入entry,如果是bucketEntry,prev为INIT(-2)或NIL(-1)时直接写入(next需要复用),
 *        反之找到freeOffset写入,更新prev,next,freeOffset
 *        hashMap满时返回false
 *    </li>
 *    <li>
 *        删除entry,如果是bucketEntry,prev更新为INIT(-2),更新key最后一个字节(+1)用于失效key
 *        反之更新prev,next,freeOffset
 *    </li>
 *    <li>
 *        segment按bucket设置分段锁bucketLocks[],freeOffset用单独的freeOffsetLock
 *        两种所都是独占非公平锁,提高吞吐量的同时也还是存在GC的问题
 *    </li>
 *  </ul>
 * </pre>
 */
public class BinaryHashMap implements Serializable {

	static final class Segment implements Serializable {

		private final static int		INIT				= -2;
		private final static int		NIL					= -1;
		private final static int		SIZE_OF_INT			= 4;

		private static final long		serialVersionUID	= 3073053149022107146L;

		private final int				capacity;
		private final int				lockDensity;
		private int						count;
		private final byte[]			entries;
		private final int				entryOffset;
		private int						freeOffset;
		private final int				indexMask;
		private final int				keyLength;
		private final int				nextOffset;
		private final int				pinOffset;
		private final int				preOffset;

		private final ReentrantLock		freeOffsetLock;
		private final ReentrantLock[]	bucketLocks;

		private final int				valueLength;
		private final int				valueOffset;

		private int						statAllocate;
		private int						statError;
		private int						statOverwrite;
		private int						statRemoveHead;
		private int						statRemoveOtherEntry;
		private int						statWriteHead;

		public Segment(final int tableCapacity, final int totalCapacity, int lockDensity, final int keyLength, final int valueLength) {
			this.keyLength = keyLength;
			this.valueLength = valueLength;
			this.capacity = totalCapacity;
			this.indexMask = tableCapacity - 1;
			this.lockDensity = lockDensity;
			this.preOffset = keyLength;
			this.nextOffset = this.preOffset + Segment.SIZE_OF_INT;
			this.valueOffset = this.nextOffset + Segment.SIZE_OF_INT;
			this.entryOffset = this.valueOffset + valueLength;
			this.pinOffset = tableCapacity * this.entryOffset;
			this.entries = new byte[this.capacity * this.entryOffset];
			this.freeOffset = this.pinOffset == this.entries.length ? Segment.NIL : this.pinOffset;
			for (int i = 0; i < this.entries.length; i += this.entryOffset) {
				if (i < this.pinOffset) {
					this.initBuckets(i);
				} else {
					this.doubleLinkFreeEntries(i);
				}
			}

			freeOffsetLock = new ReentrantLock(false);
			int locks = tableCapacity / lockDensity;
			bucketLocks = new ReentrantLock[locks];
			for (int j = 0; j < locks; j++) {
				bucketLocks[j] = new ReentrantLock(false);
			}
		}

		private void initBuckets(final int index) {
			this.writeRawInt32(Segment.INIT, index + this.preOffset);
			this.writeRawInt32(Segment.INIT, index + this.nextOffset);
		}

		private void doubleLinkFreeEntries(final int index) {
			final int pre = index == this.pinOffset ? Segment.NIL : (index - this.entryOffset);
			final int next = (index + this.entryOffset) < this.entries.length ? index + this.entryOffset : Segment.NIL;
			this.writeRawInt32(pre, index + this.preOffset);
			this.writeRawInt32(next, index + this.nextOffset);
		}

		private final boolean equals(final byte[] dst, final int offset, final int length) {
			int pos = offset;
			for (int i = 0; i < length; i++) {
				if (this.entries[pos++] != dst[i]) {
					return false;
				}
			}
			return true;
		}

		public boolean containsKey(final byte[] key, final int hash) {
			final int index = this.indexMask & hash;
			int lockSegment = index / lockDensity;
			bucketLocks[lockSegment].lock();
			try {
				final int firstEntryOffset = index * this.entryOffset;
				for (int entryOffset = firstEntryOffset;;) {
					if (this.equals(key, entryOffset, this.keyLength)) {
						return true;
					} else {
						final int entryNextOffset = entryOffset + this.nextOffset;
						entryOffset = this.readRawInt32(entryNextOffset);
						if (this.isTail(entryOffset)) {
							return false;
						}
					}
				}
			} catch (final Exception e) {
				this.statError++;
			} finally {
				bucketLocks[lockSegment].unlock();
			}
			return true;
		}

		protected boolean get(final byte[] key, final byte[] value, final int hash) {
			final int index = this.indexMask & hash;
			int lockSegment = index / lockDensity;
			bucketLocks[lockSegment].lock();
			try {
				final int firstEntryOffset = index * this.entryOffset;
				for (int entryOffset = firstEntryOffset;;) {
					if (this.equals(key, entryOffset, this.keyLength)) {
						final int entryValueOffset = entryOffset + this.valueOffset;
						this.readByte(value, entryValueOffset, this.valueLength);
						return true;
					} else {
						final int entryNextOffset = entryOffset + this.nextOffset;
						entryOffset = this.readRawInt32(entryNextOffset);
						if (this.isTail(entryOffset))
							return false;
					}
				}
			} catch (final Exception e) {
				this.statError++;
			} finally {
				bucketLocks[lockSegment].unlock();
			}
			return false;
		}

		protected boolean put(final byte[] key, final byte[] value, final int hash, final boolean onlyIfAbsent) {
			final int index = this.indexMask & hash;
			final int lockSegment = index / lockDensity;
			bucketLocks[lockSegment].lock();
			try {
				final int firstEntryOffset = index * this.entryOffset;
				for (int entryOffset = firstEntryOffset;;) {
					if (this.equals(key, entryOffset, this.keyLength)) {
						if (!onlyIfAbsent) {
							final int entryValueOffset = entryOffset + this.valueOffset;
							this.writeByte(value, entryValueOffset, this.valueLength);
						}
						this.statOverwrite++;
						return true;
					} else {
						final int entryPreOffset = entryOffset + this.preOffset;
						final int preEntryOffset = this.readRawInt32(entryPreOffset);

						final int entryNextOffset = entryOffset + this.nextOffset;
						int nextEntryOffset = this.readRawInt32(entryNextOffset);

						if (preEntryOffset == Segment.INIT) {
							if (nextEntryOffset == Segment.INIT) {
								nextEntryOffset = Segment.NIL;
							}
							this.writeEntry(key, value, Segment.NIL, nextEntryOffset, entryOffset);
							this.count++;
							this.statWriteHead++;
							return true;
							// break;
						} else if (nextEntryOffset == Segment.NIL) {
							freeOffsetLock.lock();
							try {
								if ((this.count >= this.capacity) || (this.freeOffset == Segment.NIL)) {
									return false;
								} else {
									this.count++;
								}
								final int freeEntryNextOffset = this.freeOffset + this.nextOffset;
								final int nextFreeEntryOffset = this.readRawInt32(freeEntryNextOffset);
								this.writeEntry(key, value, entryOffset, Segment.NIL, this.freeOffset);
								this.writeRawInt32(this.freeOffset, entryNextOffset);
								this.freeOffset = nextFreeEntryOffset;
								this.statAllocate++;
							} catch (Exception e) {
								statError++;
								return false;
							} finally {
								freeOffsetLock.unlock();
							}
							return true;
						} else {
							entryOffset = nextEntryOffset;
						}
					}
				}
			} catch (final Exception e) {
				this.statError++;
				// TODO
				e.printStackTrace();
			} finally {
				bucketLocks[lockSegment].unlock();
			}
			return false;
		}

		protected boolean remove(final byte[] key, final byte[] returnValue, final int hash) {
			final int index = this.indexMask & hash;
			final int lockSegment = index / lockDensity;
			bucketLocks[lockSegment].lock();
			try {
				final int firstEntryOffset = index * this.entryOffset;
				for (int entryOffset = firstEntryOffset;;) {
					if (this.equals(key, entryOffset, this.keyLength)) {
						if (entryOffset < this.pinOffset) {
							key[this.keyLength - 1] = (byte) (key[this.keyLength - 1] + 1);
							this.writeByte(key, entryOffset, this.keyLength);
							final int entryPreOffset = entryOffset + this.preOffset;
							this.writeRawInt32(Segment.INIT, entryPreOffset);

							if (returnValue != null) {
								this.readByte(returnValue, entryOffset + valueOffset, valueLength);
							}

							this.statRemoveHead++;
							this.count--;
							return true;
						} else {
							final int entryPreOffset = entryOffset + this.preOffset;
							final int preEntryOffset = this.readRawInt32(entryPreOffset);

							final int entryNextOffset = entryOffset + this.nextOffset;
							final int nextEntryOffset = this.readRawInt32(entryNextOffset);

							if (!this.isTail(nextEntryOffset)) {
								final int nextEntryPreOffset = nextEntryOffset + this.preOffset;
								this.writeRawInt32(preEntryOffset, nextEntryPreOffset);
							}

							final int preEntryNextOffset = preEntryOffset + this.nextOffset;
							this.writeRawInt32(nextEntryOffset, preEntryNextOffset);

							freeOffsetLock.lock();
							try {
								this.writeRawInt32(this.freeOffset, entryNextOffset);
								final int freeEntryPreOffset = this.freeOffset + this.preOffset;
								this.writeRawInt32(entryOffset, freeEntryPreOffset);
								this.freeOffset = entryOffset;

								if (returnValue != null) {
									this.readByte(returnValue, entryOffset + valueOffset, valueLength);
								}

								this.statRemoveOtherEntry++;
								this.count--;
								return true;
							} catch (Exception e) {
								this.statError++;
								// TODO
								e.printStackTrace();
							} finally {
								freeOffsetLock.unlock();
							}
						}
					} else {
						final int entryNextOffset = entryOffset + this.nextOffset;
						entryOffset = this.readRawInt32(entryNextOffset);
						if (this.isTail(entryOffset)) {
							return false;
						}
					}
				}
			} catch (final Exception e) {
				this.statError++;
				// TODO
				e.printStackTrace();
			} finally {
				bucketLocks[lockSegment].unlock();
			}
			return false;
		}

		private boolean isTail(final int nextEntryOffset) {
			return (nextEntryOffset == Segment.NIL) || (nextEntryOffset == Segment.INIT);
		}

		private final void readByte(final byte[] bytes, final int offset, final int length) {
			int pos = offset;
			for (int i = 0; i < length; i++) {
				bytes[i] = this.entries[pos++];
			}
		}

		private final int readRawInt32(final int offset) {
			return ((this.entries[offset] & 0xFF) | ((this.entries[offset + 1] & 0xFF) << 8) | ((this.entries[offset + 2] & 0xFF) << 16) | ((this.entries[offset + 3] & 0xFF) << 24));
		}

		@Override
		public String toString() {
			final StringBuffer sb = new StringBuffer();
			sb.append("{");
			final byte[] keyBuffer = new byte[this.keyLength];
			final byte[] valueBuffer = new byte[this.valueLength];

			final NeuronByteArrayReader keyReader = new NeuronByteArrayReader(keyBuffer, 0, keyBuffer.length);
			final NeuronByteArrayReader valueReader = new NeuronByteArrayReader(valueBuffer, 0, valueBuffer.length);

			for (int i = 0; i < this.entries.length; i += this.entryOffset) {
				keyReader.init(keyBuffer, 0, keyBuffer.length);
				valueReader.init(valueBuffer, 0, valueBuffer.length);
				try {
					this.readByte(keyBuffer, i, this.keyLength);
					this.readByte(valueBuffer, i + this.valueOffset, this.valueLength);
					sb.append("[" + i + "]\"" + keyReader.readFLong() + "\"=" + "\"" + valueReader.readFLong() + "\"~" + this.readRawInt32(i + this.preOffset) + "~" + this.readRawInt32(i + this.nextOffset));
				} catch (final IOException e) {
					e.printStackTrace();
				}
				sb.append(",");
			}
			String str = sb.substring(0, sb.length() - 1);
			str += "}";
			str += ",count=" + this.count;
			return str;
		}

		private final int writeByte(final byte[] bytes, final int offset, final int length) {
			int pos = offset;
			for (int i = 0; i < length; i++) {
				this.entries[pos++] = bytes[i];
			}
			return pos;
		}

		private final void writeEntry(final byte[] key, final byte[] value, final int pre, final int next, final int offset) {
			int pos = this.writeByte(key, offset, this.keyLength);
			pos = this.writeRawInt32(pre, pos);
			pos = this.writeRawInt32(next, pos);
			this.writeByte(value, pos, this.valueLength);
		}

		private final int writeRawInt32(final int value, final int offset) {
			int pos = offset;
			this.entries[pos++] = (byte) (value & 0xFF);
			this.entries[pos++] = (byte) ((value >> 8) & 0xFF);
			this.entries[pos++] = (byte) ((value >> 16) & 0xFF);
			this.entries[pos++] = (byte) ((value >> 24) & 0xFF);
			return pos;
		}
	}

	static final int			MAX_SEGMENTS		= 1 << 16;

	static final int			MAXIMUM_CAPACITY	= 1 << 30;

	private static final long	serialVersionUID	= -1714939166651958118L;

	final int					indexMask;

	final int					segmentCount;

	final int					segmentMask;

	final Segment[]				segments;

	final int					segmentShift;

	public BinaryHashMap(int initCapacity, int maxCapacity, int concurrencyLevel, int lockGFactor, final int klen, final int vlen) {
		// checkArgument(initialCapacity > 0 && maxCapacity > 0 &&
		// concurrencyLevel > 0);
		if (initCapacity > maxCapacity) {
			initCapacity = maxCapacity;
		}

		if (concurrencyLevel > BinaryHashMap.MAX_SEGMENTS) {
			concurrencyLevel = BinaryHashMap.MAX_SEGMENTS;
		}

		if (maxCapacity > BinaryHashMap.MAXIMUM_CAPACITY) {
			maxCapacity = BinaryHashMap.MAXIMUM_CAPACITY;
		}

		this.segmentCount = this.neighborHigherPowOf2(concurrencyLevel);
		this.segmentMask = this.segmentCount - 1;

		final int avgEntryLimit = this.avg(this.neighborHigherPowOf2(maxCapacity), this.segmentCount);
		final int avgTabLength = this.avg(this.neighborHigherPowOf2(initCapacity), this.segmentCount);

		if (avgTabLength < lockGFactor)
			throw new IllegalArgumentException();

		this.indexMask = avgTabLength - 1;
		this.segmentShift = this.neighborHigherPowOf2Shift(avgTabLength);

		final Segment[] segments = new Segment[this.segmentCount];
		for (int i = 0; i < segments.length; i++) {
			segments[i] = new Segment(avgTabLength, avgEntryLimit, lockGFactor, klen, vlen);
		}
		this.segments = segments;
	}

	private int avg(final int dividend, final int divisor) {
		return Double.valueOf(Math.ceil(dividend / divisor)).intValue();
	}

	public boolean containsKey(final byte[] key) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.containsKey(key, hash);
	}

	public boolean get(final byte[] key, final byte[] value) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.get(key, value, hash);
	}

	// CRC32 varint FINAL
	private int hash(final byte[] key) {
		int h = 0xAAAAAAAA;
		int l = 0x811C9DC5;

		if (key.length > 0) {
			for (int i = 0; i < key.length; i++) {
				final int x1 = h & 0x80000000;
				final int x2 = l & 0xFC000000;

				h <<= 1;
				l <<= 6;

				h ^= (x1 >> 31);
				l ^= (x2 >> 26);

				h ^= key[i];
				l ^= key[i];
			}
		}
		return (h << this.segmentShift) ^ (l & this.indexMask);
	}

	private int neighborHigherPowOf2(int val) {
		final int highestOneBit = Integer.highestOneBit(val);
		val = val == highestOneBit ? highestOneBit : highestOneBit << 1;
		return val;
	}

	private int neighborHigherPowOf2Shift(final int base) {
		int t = 1;
		int shift = 1;
		while (t < base) {
			++shift;
			t <<= 1;
		}
		return shift;
	}

	public boolean put(final byte[] key, final byte[] value) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.put(key, value, hash, false);
	}

	public boolean putIfAbsent(final byte[] key, final byte[] value) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.put(key, value, hash, true);
	}

	public boolean remove(final byte[] key, final byte[] returnValue) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.remove(key, returnValue, hash);
	}

	public boolean remove(final byte[] key) {
		final int hash = this.hash(key);
		final int index = this.segmentForHash(hash);
		final Segment segment = this.segments[index];
		return segment.remove(key, null, hash);
	}

	private int segmentForHash(final int hash) {
		return (hash >>> this.segmentShift) & this.segmentMask;
	}

	public int size() {
		int count = 0;
		for (int i = 0; i < this.segments.length; i++) {
			count += this.segments[i].count;
		}
		return count;
	}

	@Override
	public String toString() {
		int count = 0;
		int remove = 0;
		int capacity = 0;

		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < this.segments.length; i++) {
			Segment segment = this.segments[i];
			sb.append(i);
			sb.append(":{");
			sb.append("\"capacity\":");
			sb.append(segment.capacity);

			sb.append(", \"count\":");
			sb.append(segment.count);

			sb.append(", \"override\":");
			sb.append(segment.statOverwrite);

			sb.append(", \"allocate\":");
			sb.append(segment.statAllocate);

			sb.append(", \"writeHead\":");
			sb.append(segment.statWriteHead);

			sb.append(", \"tabLength\":");
			sb.append(segment.indexMask + 1);

			sb.append(", \"freeOffset\":");
			sb.append(segment.freeOffset);

			sb.append(", \"removeHead\":");
			sb.append(segment.statRemoveHead);

			sb.append(", \"error\":");
			sb.append(segment.statError);

			sb.append("}\n");

			count += segment.count;
			capacity += segment.capacity;
			remove += segment.statRemoveHead + segment.statRemoveOtherEntry;
		}

		sb.append("\"sum\":{");
		sb.append("\"count\":");
		sb.append(count);

		sb.append(", \"remove\":");
		sb.append(remove);

		sb.append(", \"capacity\":");
		sb.append(capacity);
		sb.append("}");

		return sb.toString();
	}

	public String toDebugString() {
		int count = 0;
		String str = "BinaryHashMap{" + "segmentMask=" + this.segmentMask + ", segmentShift=" + this.segmentShift + ", segments num=" + this.segments.length + ",\n";
		for (int i = 0; i < this.segments.length; i++) {
			str += i + "=" + this.segments[i].toString() + "\n";
			count += this.segments[i].count;
		}
		str += ",count=" + count + "}";
		return str;
	}

}
