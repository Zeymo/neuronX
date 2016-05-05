package io.zeymo.cache.statistics;

/**
 * Created By Zeymo at 14-8-14 14:45
 */
public interface CacheUsageListener {

	void notifyEvict();

	void notifyGetTimeNanos(final long nanos);

	void notifyHit();

	void notifyMiss();

	void notifyPut();

	void notifyStatisticsCleared();

	void notifyStatisticsEnabledChanged(boolean enableStatistics);

	void notifyTimeTakenForGet(final long nanos);

}
