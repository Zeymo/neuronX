package io.zeymo.cache.event;

import io.zeymo.cache.DirectMemoryCache;

public interface CacheEventListener extends Cloneable {

	public Object clone() throws CloneNotSupportedException;

	void notifyElementEvicted(final DirectMemoryCache cache, final long key);

	void notifyElementExpired(final DirectMemoryCache cache, final long key);

	void notifyElementPut(final DirectMemoryCache cache, final long key);

	void notifyElementRemoved(final DirectMemoryCache cache, final long key);

	void notifyElementUpdated(final DirectMemoryCache cache, final long key);

}
