package io.zeymo.cache.memory;


import io.zeymo.cache.CacheAccessor;
import io.zeymo.cache.CacheConstants;
import io.zeymo.cache.CacheParam;
import io.zeymo.cache.DirectMemoryCache;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created By Zeymo at 14-8-7 07:27
 */
public class SlabMaster extends ArrangedContainer {

	private final int[]		slabBlockSize;
	private final int		slabCount;
	private final Slab[]	slabInstance;
	private final int		keyLength;

	public SlabMaster(final List<Slab> slabs, final int keyLength) {
		this.keyLength = keyLength;
		Collections.sort(slabs, new Comparator<Slab>() {
			@Override
			public int compare(Slab o1, Slab o2) {
				int a = o1.getBlockSize();
				int b = o2.getBlockSize();

				return (a < b) ? -1 : ((a > b) ? 1 : 0);
			}
		});

		this.slabCount = slabs.size();
		this.slabBlockSize = new int[slabCount];
		this.slabInstance = new Slab[slabCount];

		// Preconditions.checkArgument((slabs != null) && (slabCount > 0),
		// "slabs must not be empty!");

		int index = 0;
		for (Slab slab : slabs) {
			this.slabBlockSize[index] = slab.getBlockSize();
			this.slabInstance[index] = slab;
			++index;
		}

	}

	@Override
	public int capacity() {
		int totalSize = 0;
		for (final Slab slab : this.slabInstance) {
			totalSize += slab.capacity();
		}
		return totalSize;
	}

	@Override
	public void clear() {
		for (final Slab slab : this.slabInstance) {
			slab.clear();
		}
	}

	public void remove(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, final CacheParam param, final boolean handleEvict) {
		final Slab slab = this.slabInstance[p.getSlabIndex()];
		slab.remove(accessor, p, param, handleEvict);
	}

	public void get(final DirectMemoryCache.Pointer p, final CacheParam param, final boolean checkPin, final boolean setPin) {
		final Slab slab = this.slabInstance[p.getSlabIndex()];
		slab.get(p, param, checkPin, setPin);
	}

	public void put(final CacheAccessor accessor, final DirectMemoryCache.Pointer p, final CacheParam param, final boolean checkPin, final boolean setPin) {
		final Slab slab = this.slabInstance[p.getSlabIndex()];
		slab.put(accessor, p, param, checkPin, setPin);
	}

	public void route(final DirectMemoryCache.Pointer p, final CacheParam param) {
		// 这里的CacheConstants.SIZE_OF_INT是vlen的length
		final int valueLength = param.getValueLength();
		final int ele = keyLength + valueLength + CacheConstants.SIZE_OF_INT;

		final int pos = this.position(ele);
		final Slab slab = this.slabInstance[pos];
		slab.route(p, param);
		p.setSlabPos((short) pos);
		// p.setValueLength(vlen);
	}

	public boolean pin(final DirectMemoryCache.Pointer p, final CacheParam param) {
		final Slab slab = this.slabInstance[p.getSlabIndex()];
		return slab.pin(p, param);
	}

	public boolean unPin(final DirectMemoryCache.Pointer p, final CacheParam param) {
		final Slab slab = this.slabInstance[p.getSlabIndex()];
		return slab.unPin(p, param);
	}

	private int position(final int val) {
		final int index = CacheConstants.NIL;
		for (int i = 0; i < this.slabCount; i++) {
			if (val <= this.slabBlockSize[i]) {
				return i;
			}
		}
		return index;
	}
}
