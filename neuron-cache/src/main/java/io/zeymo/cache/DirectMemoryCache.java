package io.zeymo.cache;

import com.google.common.collect.Lists;
import io.zeymo.cache.memory.Segment;
import io.zeymo.cache.memory.Slab;
import io.zeymo.cache.memory.SlabMaster;
import io.zeymo.map.BinaryHashMap;

import java.util.List;

/**
 * Created By Zeymo at 14-8-7 09:50
 */
public class DirectMemoryCache {

	public final static class Pointer {
		public final static int	SIZE_OF_POINTER	= CacheConstants.SIZE_OF_BYTE * 2 + CacheConstants.SIZE_OF_INT;
		private final byte[]	sharedKey;
		private final byte[]	source;
		private final byte[]	nullKey;
		/**
		 * 任何 get/put/remove @ cache 操作成功时，blockPos != NIL
		 */
		private int				blockPos		= CacheConstants.NIL;
		private int				segmentIndex	= CacheConstants.NIL;
		private int				slabIndex		= CacheConstants.NIL;
		private int				vlen;

		public Pointer(final int keyLength) {
			this.source = new byte[Pointer.SIZE_OF_POINTER];
			this.sharedKey = new byte[keyLength];
			this.nullKey = new byte[keyLength];
			for (int i = 0; i < keyLength; ++i) {
				nullKey[i] = -1;
			}
		}

		public void clear() {
			this.slabIndex = CacheConstants.NIL;
			this.segmentIndex = CacheConstants.NIL;
			this.blockPos = CacheConstants.NIL;
			this.vlen = CacheConstants.NIL;
		}

		public void fromSource() {
			this.slabIndex = this.source[0] & 0xFF;

			this.segmentIndex = this.source[1] & 0xFF;

			this.blockPos = (this.source[2] & 0xFF) //
					| ((this.source[3] & 0xFF) << 8) //
					| ((this.source[4] & 0xFF) << 16) //
					| ((this.source[5] & 0xFF) << 24);
		}

		public int getBlockPos() {
			return this.blockPos;
		}

		public int getSegmentIndex() {
			return this.segmentIndex;
		}

		public byte[] getSharedKey() {
			return this.sharedKey;
		}

		public int getSlabIndex() {
			return this.slabIndex;
		}

		public byte[] getSource() {
			return this.source;
		}

		public int getVlen() {
			return vlen;
		}

		public void setVlen(int vlen) {
			this.vlen = vlen;
		}

		// public boolean isShareKeyEqualsNone() {
		// for (int i = 0; i < this.sharedKey.length; i++) {
		// if (this.sharedKey[i] != 0) {
		// return false;
		// }
		// }
		// return true;
		// }

		public boolean isSourceEqualsNone() {
			for (int i = 0; i < this.source.length; i++) {
				if (this.source[i] != 0) {
					return false;
				}
			}
			return true;
		}

		public void reset(final int slabPos, final int segmentPos, final int valueLength) {
			setSlabPos(slabPos);
			setSegmentPos(segmentPos);
			setVlen(valueLength);
			setBlockPos(CacheConstants.NIL);
		}

		public void setBlockPos(final int blockPos) {
			this.blockPos = blockPos;
		}

		public void setSegmentPos(final int segmentPos) {
			this.segmentIndex = segmentPos;
		}

		public void setSlabPos(final int slabPos) {
			this.slabIndex = slabPos;
		}

		public byte[] getNullKey() {
			return nullKey;
		}

		// public boolean sharedKeyEquals(final byte[] key) {
		// for (int i = 0; i < this.sharedKey.length; i++) {
		// if (this.sharedKey[i] != key[i]) {
		// return false;
		// }
		// }
		// return true;
		// }

		public byte[] toSource() {
			this.source[0] = (byte) (this.slabIndex & 0xFF);
			this.source[1] = (byte) (this.segmentIndex & 0xFF);
			this.source[2] = (byte) (this.blockPos & 0xFF);
			this.source[3] = (byte) ((this.blockPos >> 8) & 0xFF);
			this.source[4] = (byte) ((this.blockPos >> 16) & 0xFF);
			this.source[5] = (byte) ((this.blockPos >> 24) & 0xFF);
			return this.source;
		}

		@Override
		public String toString() {
			return "Pointer{" + "blockPos=" + blockPos + ", segmentIndex=" + segmentIndex + ", slabIndex=" + slabIndex + '}';
		}
	}

	private final SlabMaster cacheService;
	private final int			klen;
	private final BinaryHashMap metadata;

	public DirectMemoryCache(final CacheLayout layout) {

		int keyLength = layout.getKeyLength();

		int indexCapacity = layout.getIndexCapacity();
		int indexConflict = layout.getIndexConflict();

		int maxCapacity = indexCapacity + indexConflict;
		int concurrency = layout.getConcurrencyLevel();
		int lockDensity = layout.getIndexLockDensity();

		this.klen = keyLength;

		this.metadata = new BinaryHashMap(indexCapacity, maxCapacity, concurrency, lockDensity, keyLength, DirectMemoryCache.Pointer.SIZE_OF_POINTER);

		final List<Slab> slabs = Lists.newArrayList();

		int slabIndex = 0;

		for (CacheLayout.SegmentLayout segmentConfiguration : layout.getSegmentConfigurationList()) {

			int blockSize = segmentConfiguration.getBlockSize();
			int segmentCount = segmentConfiguration.getSegmentCount();
			int segmentSize = segmentConfiguration.getSegmentSize();

			final Segment[] segments = new Segment[segmentCount];

			for (int segmentIndex = 0; segmentIndex < segmentCount; segmentIndex++) {
				Segment segment = new Segment(segmentIndex, segmentSize, blockSize, keyLength);
				segments[segmentIndex] = segment;
			}

			slabs.add(new Slab(slabIndex, segments, blockSize, keyLength));
			++slabIndex;
		}
		this.cacheService = new SlabMaster(slabs, keyLength);
	}

	protected boolean remove(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, final CacheParam param) {
		final byte[] keyBytes = param.getKey();

		if (this.metadata.remove(keyBytes, p.getSource())) {
			p.fromSource();
			this.cacheService.remove(accessor, p, param, true);
			return param.isEvicted();
		}

		// if ( this.metadata.get(keyBytes, p.getSource()) ) {
		// p.fromSource();
		// this.cacheService.remove(p, param);
		// return this.metadata.remove(keyBytes);
		// }

		return false;
	}

	protected void get(final DirectMemoryCache.Pointer p, CacheParam param, final boolean checkPin, final boolean setPin) {
		if (this.metadata.get(param.getKey(), p.getSource())) {
			p.fromSource();
			this.cacheService.get(p, param, checkPin, setPin);
		}
	}

	protected void put(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, final CacheParam param, final boolean checkPin, final boolean setPin) {
		boolean changeStore = false;
		this.cacheService.route(p, param);

		final byte[] keyBytes = param.getKey();

		if (this.metadata.get(keyBytes, p.getSource())) {
			final int slabIndex = p.getSlabIndex();
			final int segmentPos = p.getSegmentIndex();
			p.fromSource();

			if (slabIndex != p.getSlabIndex()) {
				this.cacheService.remove(accessor, p, param, false);
				p.reset(slabIndex, segmentPos, param.getValueLength());
				changeStore = true;
			}
		}

		// TODO re-check?

		this.cacheService.put(accessor, p, param, checkPin, setPin);
		if (p.getBlockPos() != CacheConstants.NIL) {
			if (!changeStore) {
				this.metadata.putIfAbsent(keyBytes, p.toSource());
			} else {
				this.metadata.put(keyBytes, p.toSource());
			}

			if (param.isEvicted()) {
				// 删除被evict的元素
				this.metadata.remove(param.getEvictKey());
			}
		}
	}

	protected boolean pin(final DirectMemoryCache.Pointer p, CacheParam param) {
		this.cacheService.route(p, param);
		return this.cacheService.pin(p, param);
	}

	protected boolean unPin(final DirectMemoryCache.Pointer p, CacheParam param) {
		this.cacheService.route(p, param);
		return this.cacheService.unPin(p, param);
	}

	public int getKLen() {
		return this.klen;
	}

	public BinaryHashMap getMetadata() {
		return metadata;
	}

}