package io.zeymo.cache.statistics;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created By Zeymo at 14-8-14 15:11
 */
public class Statistics implements CacheStatistics, CacheStatisticsEvent {

	public final static Statistics	instance				= new Statistics();

	private static final int		MIN_MAX_DEFAULT_VALUE	= -1;
	private static final int		MILLIS_PER_SECOND		= 1000;
	private static final int		NANOS_PER_MILLI			= Statistics.MILLIS_PER_SECOND * Statistics.MILLIS_PER_SECOND;
	private static final int		HIT_RATIO_MULTIPLIER	= 100;

	private final AtomicBoolean		statisticsEnabled		= new AtomicBoolean(true);

	private final AtomicLong		hitCount				= new AtomicLong(0);
	private final AtomicLong		missCount				= new AtomicLong(0);
	private final AtomicLong		evictedCount			= new AtomicLong(0);
	private final AtomicLong		putCount				= new AtomicLong(0);
	private final AtomicLong		totalGetTimeTakenNanos	= new AtomicLong(0);
	private final AtomicLong		minGetTimeNanos			= new AtomicLong(Statistics.MIN_MAX_DEFAULT_VALUE);
	private final AtomicLong		maxGetTimeNanos			= new AtomicLong(Statistics.MIN_MAX_DEFAULT_VALUE);

	// private final List<CacheUsageListener> listeners = new
	// CopyOnWriteArrayList<CacheUsageListener>();

	@Override
	public void addGetTimeNanos(final long nanos) {
		if (!this.statisticsEnabled.get()) {
			return;
		}
		this.totalGetTimeTakenNanos.addAndGet(nanos);
		if ((this.minGetTimeNanos.get() == Statistics.MIN_MAX_DEFAULT_VALUE) || (nanos < this.minGetTimeNanos.get())) {
			this.minGetTimeNanos.set(nanos);
		}
		if ((this.maxGetTimeNanos.get() == Statistics.MIN_MAX_DEFAULT_VALUE) || ((nanos > this.maxGetTimeNanos.get()) && (nanos > 0))) {
			this.maxGetTimeNanos.set(nanos);
		}
	}

	@Override
	public void cacheEvicted() {
		if (!this.statisticsEnabled.get()) {
			return;
		}
		this.evictedCount.incrementAndGet();
	}

	@Override
	public void cacheHit() {
		if (!this.statisticsEnabled.get()) {
			return;
		}
		this.hitCount.incrementAndGet();
	}

	@Override
	public void cacheMiss() {
		if (!this.statisticsEnabled.get()) {
			return;
		}
		this.missCount.incrementAndGet();
	}

	@Override
	public void clearStatistics() {
		this.hitCount.set(0);
		this.missCount.set(0);
		this.evictedCount.set(0);
		this.putCount.set(0);
		this.totalGetTimeTakenNanos.set(0);
		this.minGetTimeNanos.set(Statistics.MIN_MAX_DEFAULT_VALUE);
		this.maxGetTimeNanos.set(Statistics.MIN_MAX_DEFAULT_VALUE);
	}

	@Override
	public long getAverageGetTimeNanos() {
		final long accessCount = this.getCacheHitCount() + this.getCacheMissCount();
		if (accessCount == 0) {
			return 0;
		}
		return this.totalGetTimeTakenNanos.get() / accessCount;
	}

	@Override
	public long getCacheHitCount() {
		return this.hitCount.get();
	}

	@Override
	public int getCacheHitRatio() {
		final long hits = this.getCacheHitCount();
		final long accesses = hits + this.getCacheMissCount();
		return (int) (accesses == 0 ? 0 : ((hits / (double) accesses) * Statistics.HIT_RATIO_MULTIPLIER));
	}

	@Override
	public long getCacheMissCount() {
		return this.missCount.get();
	}

	@Override
	public long getEvictCount() {
		return this.evictedCount.get();
	}

	@Override
	public long getMaxGetTimeNanos() {
		return this.maxGetTimeNanos.get() / Statistics.NANOS_PER_MILLI;
	}

	@Override
	public long getMinGetTimeNanos() {
		return this.minGetTimeNanos.get() / Statistics.NANOS_PER_MILLI;
	}

	@Override
	public long getPutCount() {
		return this.putCount.get();
	}

	@Override
	public boolean isStatisticsEnabled() {
		return this.statisticsEnabled.get();
	}

	@Override
	public void setStatisticsEnabled(final boolean enableStatistics) {
		if (enableStatistics) {
			this.clearStatistics();
		}
		this.statisticsEnabled.set(enableStatistics);
	}
}
