package io.zeymo.cache.statistics;

public interface CacheStatisticsEvent {

	public void addGetTimeNanos(final long nanos);

	public void cacheEvicted();

	public void cacheHit();

	public void cacheMiss();

	public void clearStatistics();

	public void setStatisticsEnabled(final boolean enableStatistics);

}
