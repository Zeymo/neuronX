package io.zeymo.cache;

public abstract class CacheAccessor {

	// 【cache遗留问题】
	//
	// 现在在put操作遇到数据膨胀时
	// 1、读map
	// 2、删除原有数据
	// 3、写入新数据
	// 4、写入新meta到map
	//
	// 相同Key数据出现这样的时序会死掉：
	// A: 1-23----4
	// B: -1--23-4-
	//
	// 不过现在这个问题在Neuron场景中不会出现：因为Engine.EventBuffers是按照NodeID做路由，因此任意多个Key相同的Node不会出现时序交叉的情况

	public static final class DefaultCacheAccessorImpl extends CacheAccessor {
		public DefaultCacheAccessorImpl(DirectMemoryCache cache) {
			super(cache);
		}

		@Override
		public void handleEvict(CacheParam cacheParam, boolean isRemove) {
			// NOTHING
		}
	}

	private final DirectMemoryCache			cache;
	private final DirectMemoryCache.Pointer	pointer;

	public CacheAccessor(final DirectMemoryCache cache) {
		this.cache = cache;
		this.pointer = new DirectMemoryCache.Pointer(cache.getKLen());
	}

	public boolean get(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.get(this.pointer, cacheParam, false, false);

		cacheParam.setValueLength(this.pointer.getVlen());

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	public boolean getWithPin(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.get(this.pointer, cacheParam, false, true);

		int length = this.pointer.getVlen();
		cacheParam.setValueLength(length);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	public boolean getWithUnPin(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.get(this.pointer, cacheParam, true, false);
		int length = this.pointer.getVlen();
		cacheParam.setValueLength(length);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	/**
	 * handle在缓存操作内部最深处（lock保护的一个segment-slot）触发的evict事件<br>
	 * cache保证在这个过程中不会出现其他线程访问到这个segment<br>
	 * 【注意】绝对不能抛异常, 我会帮你封装个Error扔到最外面，开心吧
	 * 
	 * @param cacheParam
	 */
	public abstract void handleEvict(CacheParam cacheParam, boolean isRemove);

	public boolean pin(final CacheParam cacheParam) {
		this.pointer.clear();
		return this.cache.pin(this.pointer, cacheParam);
	}

	public boolean put(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.put(this, this.pointer, cacheParam, false, false);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	public boolean putWithPin(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.put(this, this.pointer, cacheParam, false, true);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	public boolean putWithUnPin(final CacheParam cacheParam) {
		this.pointer.clear();
		this.cache.put(this, this.pointer, cacheParam, true, false);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

	public boolean remove(final CacheParam param) {
		this.pointer.clear();
		this.cache.remove(this, this.pointer, param);

		int position = this.pointer.getBlockPos();
		return position != CacheConstants.NIL;
	}

}