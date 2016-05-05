package io.zeymo.cache.memory;

import com.google.common.base.Preconditions;
import io.zeymo.cache.CacheAccessor;
import io.zeymo.cache.CacheParam;
import io.zeymo.cache.DirectMemoryCache;

/**
 * Created By Zeymo at 14-8-6 16:19
 */
public class Slab extends ArrangedContainer {

	private final Segment[]	segments;

	private final int		blockSize;
	private final int		segmentCount;
	private final int		keyLength;

	public int getBlockSize() {
		return this.blockSize;
	}

	// private final int segmentMask;

	public Slab(final int slabIndex, final Segment[] segments, final int blockSize, final int keyLength) {
		super(slabIndex);
		this.keyLength = keyLength;
		this.blockSize = blockSize;
		Preconditions.checkArgument(segments.length > 0, "allocated segment size must be greater than 0!");
		this.segments = segments;
		this.segmentCount = segments.length;
		// this.segmentMask = segments.length - 1;
	}

	@Override
	public int capacity() {
		int total = 0;
		for (final Segment segment : this.segments) {
			total += segment.capacity();
		}
		return total;
	}

	@Override
	public void clear() {
		for (final Segment segment : this.segments) {
			segment.clear();
		}
	}

	public void remove(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, final CacheParam param, final boolean handleEvict) {
		final Segment segment = this.segments[p.getSegmentIndex()];
		segment.remove(accessor, p, param, handleEvict);
	}

	public void get(final DirectMemoryCache.Pointer p, CacheParam param, final boolean checkPin, final boolean setPin) {
		final Segment segment = this.segments[p.getSegmentIndex()];
		segment.get(p, param, checkPin, setPin);
	}

	public void put(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, CacheParam param, final boolean checkPin, final boolean setPin) {
		final Segment segment = this.segments[p.getSegmentIndex()];
		segment.put(accessor, p, param, checkPin, setPin);
	}

	public void route(final DirectMemoryCache.Pointer p, final CacheParam param) {
		final int segmentIndex = this.hash(param.getKey()) % this.segmentCount;
		Preconditions.checkNotNull(this.segments[segmentIndex], "allocated segment must not be null!");
		p.setSegmentPos((short) segmentIndex);
	}

	public boolean pin(final DirectMemoryCache.Pointer p, final CacheParam param) {
		final Segment segment = this.segments[p.getSegmentIndex()];
		final int blockNumber = p.getBlockPos() / this.blockSize;
		return segment.pin(blockNumber, p, param);
	}

	public boolean unPin(final DirectMemoryCache.Pointer p, final CacheParam param) {
		final Segment segment = this.segments[p.getSegmentIndex()];
		return segment.unPin(p.getBlockPos() / this.blockSize, p, param);
	}

	private int hash(final byte[] key) {
		int l = 0x811C9DC5;
		if (key.length > 0) {
			for (int i = 0; i < keyLength; i++) {
				final int x2 = l & 0xFC000000;
				l <<= 6;
				l ^= (x2 >> 26);
				l ^= key[i];
			}
		}
		return (l & 0x7FFFFFFF);
	}
}