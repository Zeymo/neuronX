package io.zeymo.cache.memory;

import com.google.common.base.Preconditions;
import io.zeymo.cache.CacheAccessor;
import io.zeymo.cache.CacheConstants;
import io.zeymo.cache.CacheParam;
import io.zeymo.cache.DirectMemoryCache;
import io.zeymo.commons.utils.ByteUtils;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created By Zeymo at 14-8-6 16:19 ByteBuffer-Based-GC-Free-FixSize-DirectMemoryCache BlockCache和LRUBlocks配合使用 LRUBlocks.index * blockSize 即为 BlockCache.pos
 * 
 * <pre>
 *  <ul>
 *    <p>BlockCache</p>
 *    <graph>
 *     pos
 *     +---——--+-------+-------+-------+-------+-------+
 *     | block | block | block | block | block | block |
 *     +----—--+-------+-------+-------+-------+-------+
 *       unpin   unpin   unpin   unpin   unpin   unpin
 * 
 *    offset  klen    4      vlen
 *          +---——-+------+------+
 *    block | key  | vlen | value|
 *          +----—-+------+------+
 *    </graph>
 *    <li>pin表示该entry被锁定不能呗alloc,反之unpin,初始化为unpin</li>
 *    <li>每次alloc从tail往前找第一个unpin的index</li>
 *    <li>pos = LRUBlocks.index * blockSize</li>
 *    <li>判断pointer中得blockPos是否位NIL,判断key并insertOrUpdate</li>
 *  </ul>
 *  <span>LRUBlocks</span>
 *   <span>
 *           head                                  tail
 *     +---——-+------+ __ +------+------+ __ +------+------+
 *     | next | prev | __ | next | prev | __ | next | prev |
 *     +----—-+------+    +------+------+    +------+------+
 *    </span>
 *  <ul>
 *    <li>next和prev代表前后关联的index</li>
 *    <li>初始化时用i-1和i+1初始化next和prev</li>
 *  </ul>
 *  <ul>
 *      即使把BlockCache设置成duplicate操作LRUBlocks也无法并发,所以还是整个segment用了一个独占非公平锁;
 *      无法和索引{@link io.zeymo.map.BinaryHashMap}
 *      进行原子操作,有数据和期望不一致问题但不是脏数据,后续设置新的锁策略
 *  </ul>
 * </pre>
 */
public class Segment extends ArrangedContainer {

	private final static int	NIL		= -1;
	private final static byte	PIN		= 1;
	private final static byte	UNPIN	= 0;
	private final ByteBuffer	blockCache;
	private final int			blockNum;
	private final int			blockSize;
	private int					head	= 0;
	private final ReentrantLock	lock;
	private final int[]			lruBlocks;
	private final byte[]		pinStates;
	private final int			segmentSize;
	private int					tail	= 0;
	private final int			keyLength;

	public Segment(final int uid, final int segmentSize, final int blockSize, final int keyLength) {
		super(uid);

		Preconditions.checkArgument((segmentSize > 0) && (segmentSize <= Integer.MAX_VALUE), "allocated segment size must be greater than 0 and less than 2G!");
		this.keyLength = keyLength;
		this.segmentSize = segmentSize;
		this.blockSize = blockSize;
		this.blockNum = segmentSize / blockSize;
		this.lruBlocks = new int[this.blockNum * 2];
		this.pinStates = new byte[this.blockNum];
		this.blockCache = ByteBuffer.allocateDirect(segmentSize);
		this.lock = new ReentrantLock(false);
		this.linkFreeBlocksAndInitCursor();
	}

	private int allocate() {
		int pre_allocate_tail = this.tail;
		while (this.pinStates[pre_allocate_tail] == Segment.PIN) {
			pre_allocate_tail = this.lruBlocks[pre_allocate_tail << 1];
		}
		return pre_allocate_tail;
	}

	@Override
	public int capacity() {
		return this.segmentSize;
	}

	@Override
	public void clear() {
		// lruBlocks = new int[blockNum];
	}

	public void compareAndSetPin(final int blockNumber, final boolean checkPin, final boolean setPin) {
		if (checkPin == setPin) {
			return;
		}
		this.lock.lock();
		try {
			if (checkPin == false) {
				if (this.pinStates[blockNumber] == Segment.UNPIN) {
					this.pinStates[blockNumber] = Segment.PIN;
				}
			} else {
				if (this.pinStates[blockNumber] == Segment.PIN) {
					this.pinStates[blockNumber] = Segment.UNPIN;
				}
			}
		} finally {
			this.lock.unlock();
		}
	}

	private void demote(final int cur) {
		if (cur != this.tail) {
			final int left = cur << 1;
			final int right = left + 1;
			if (cur == head) {
				head = this.lruBlocks[right];
			} else {
				this.lruBlocks[(this.lruBlocks[left] << 1) + 1] = this.lruBlocks[right];
			}
			this.lruBlocks[(this.lruBlocks[right]) << 1] = this.lruBlocks[left];
			this.lruBlocks[left] = tail;
			this.lruBlocks[right] = Segment.NIL;
			this.lruBlocks[(tail << 1) + 1] = cur;
			tail = cur;
		}
	}

	public void get(final DirectMemoryCache.Pointer p, final CacheParam param, final boolean checkPin, final boolean setPin) {
		this.lock.lock();
		try {
			final int blockPosition = p.getBlockPos();

			this.blockCache.position(blockPosition);
			this.blockCache.limit(blockPosition + this.blockSize);

			this.blockCache.get(p.getSharedKey(), 0, keyLength);

			final byte[] keyBytes = param.getKey();
			if (ByteUtils.compare(p.getSharedKey(), 0, keyBytes, 0, keyLength)) {
				final int vlen = this.blockCache.getInt();// add by Zeymo at 14-9-29 17:35 增加vlen
				this.blockCache.get(param.getValue(), 0, vlen);

				p.setVlen(vlen);

				final int blockNumber = blockPosition / this.blockSize;
				this.promote(blockNumber);
				this.compareAndSetPin(blockNumber, checkPin, setPin);
			} else {
				// 数据被操作间隙中并发修改
				p.setBlockPos(CacheConstants.NIL);
				return;
			}
			// else{
			// ++ p.vLen always initialized with -1
			// p.setVlen(NIL);
			// }
		} catch (final Exception e) {
			// e.printStackTrace();
		} finally {
			this.blockCache.clear();
			this.lock.unlock();
		}
	}

	private void linkFreeBlocksAndInitCursor() {
		for (int i = 0; i < this.blockNum; i++) {
			this.lruBlocks[i << 1] = i - 1;
			this.lruBlocks[(i << 1) + 1] = i + 1;
			this.pinStates[i] = Segment.UNPIN;
		}
		lruBlocks[lruBlocks.length - 1] = NIL;
		this.tail = this.blockNum - 1;
	}

	public boolean pin(final int blockNumber, final DirectMemoryCache.Pointer pointer, final CacheParam param) {
		this.lock.lock();
		try {
			this.blockCache.get(pointer.getSharedKey(), 0, keyLength);
			if (ByteUtils.compare(pointer.getSharedKey(), 0, param.getKey(), 0, keyLength)) {
				this.pinStates[blockNumber] = Segment.PIN;
				return true;
			}
			return false;
		} finally {
			this.lock.unlock();
		}
	}

	private void promote(final int cur) {
		if (cur != this.head) {
			final int left = cur << 1;
			final int right = left + 1;

			this.lruBlocks[(this.lruBlocks[left] << 1) + 1] = this.lruBlocks[right];

			if (cur == this.tail) {
				this.tail = this.lruBlocks[left];
			} else {
				this.lruBlocks[(this.lruBlocks[right]) << 1] = this.lruBlocks[left];
			}
			this.lruBlocks[left] = Segment.NIL;
			this.lruBlocks[right] = this.head;
			this.lruBlocks[this.head << 1] = cur;
			this.head = cur;
		}
	}

	public void put(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, CacheParam param, final boolean checkPin, final boolean setPin) {
		this.lock.lock();
		try {
			final int blockPosition = p.getBlockPos();
			final int blockNumber;
			final byte[] keyBytes = param.getKey();
			final byte[] valueBytes = param.getValue();
			final int valueLength = param.getValueLength();

			if (blockPosition != Segment.NIL) {
				// 更新相同key下的已有数据

				this.blockCache.position(blockPosition);
				this.blockCache.limit(blockPosition + this.blockSize);

				this.blockCache.get(p.getSharedKey(), 0, keyLength);
				blockNumber = blockPosition / this.blockSize;

				if (ByteUtils.compare(p.getSharedKey(), 0, param.getKey(), 0, keyLength)) {

					this.blockCache.putInt(valueLength);// add by Zeymo at 14-9-29 17:35 增加vlen
					this.blockCache.put(valueBytes, 0, valueLength);
					this.promote(blockNumber);
				} else {
					// 数据被操作间隙中并发修改
					p.setBlockPos(CacheConstants.NIL);
					return;
				}

			} else {
				// evict其他KeyValue数据

				final int pre_allocate_tail = this.allocate();
				this.promote(pre_allocate_tail);
				blockNumber = pre_allocate_tail;

				final int position = pre_allocate_tail * this.blockSize;
				p.setBlockPos(position);

				this.blockCache.position(position);
				this.blockCache.limit(position + this.blockSize);
				this.blockCache.mark();

				// evict happened
				blockCache.get(param.getEvictKey(), 0, keyLength);

				int length = blockCache.getInt();
				if (length > 0) {
					param.setEvicted(true);

					blockCache.get(param.getEvictValue(), 0, length);
					param.setEvictValueLength(length);

					try {
						accessor.handleEvict(param, false);
					} catch (Throwable t) {
						// FATAL: should never happen, no log
						throw new Error("evict handler should not throw exception.", t);
					}
				}

				// offset klen 4 vlen
				// ~~~~~~~ +---—-+------+-------+
				// block | key | vlen | value |
				// ~~~~~~~ +-----+------+-------+
				// this.blockCache.get(p.getSharedKey(), 0, keyLength);

				this.blockCache.reset();
				this.blockCache.put(keyBytes, 0, keyLength);
				this.blockCache.putInt(valueLength);// add Zeymo at 14-9-29 17:35 增加vlen
				this.blockCache.put(valueBytes, 0, valueLength);

			}
			this.compareAndSetPin(blockNumber, checkPin, setPin);

		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			this.blockCache.clear();
			this.lock.unlock();
		}
	}

	public void remove(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, CacheParam param, final boolean handleEvict) {
		this.lock.lock();
		try {
			this.blockCache.position(p.getBlockPos());
			this.blockCache.limit(p.getBlockPos() + this.blockSize);
			this.blockCache.mark();

			// offset klen 4 vlen
			// +----——-+-----+------+------+
			// | BLOCK | key | vlen | value|
			// +-----—-+-----+------+------+

			// evict
			blockCache.get(param.getEvictKey(), 0, keyLength);
			int length = blockCache.getInt();

			if (ByteUtils.compare(param.getEvictKey(), 0, param.getKey(), 0, keyLength)) {

				if (length > 0) {
					param.setEvicted(true);
					blockCache.get(param.getEvictValue(), 0, length);
					param.setEvictValueLength(length);

					// 保证
					try {
						accessor.handleEvict(param, false);
					} catch (Throwable t) {
						// FATAL: should never happen, no log
						throw new Error("evict handler should not throw exception.", t);
					}
				}

				this.blockCache.reset();
				this.blockCache.put(p.getNullKey(), 0, keyLength);
				this.blockCache.putInt(0);

				demote(p.getBlockPos() / this.blockSize);
			}
		} catch (final Exception e) {
			// e.printStackTrace();
		} finally {
			this.blockCache.clear();
			this.lock.unlock();
		}
	}

	public boolean unPin(final int blockNumber, final DirectMemoryCache.Pointer pointer, CacheParam param) {
		this.lock.lock();
		try {
			this.blockCache.get(pointer.getSharedKey(), 0, keyLength);
			if (ByteUtils.compare(pointer.getSharedKey(), 0, param.getKey(), 0, keyLength)) {
				this.pinStates[blockNumber] = Segment.UNPIN;
				return true;
			}
			return false;
		} finally {
			this.lock.unlock();
		}
	}
}
