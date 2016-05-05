package io.zeymo.cache.statistics;

public interface CacheStatistics {

	public long getAverageGetTimeNanos();

	public long getCacheHitCount();

	public int getCacheHitRatio();

	public long getCacheMissCount();

	public long getEvictCount();

	public long getMaxGetTimeNanos();

	public long getMinGetTimeNanos();

	public long getPutCount();

	public boolean isStatisticsEnabled();
}
