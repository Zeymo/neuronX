package io.zeymo.cache;

import com.google.common.base.Preconditions;
import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Created By Zeymo at 14-8-7 09:51
 */
public class CacheLayout implements JsonPropertyConfigurable {

	public static class SegmentLayout {

		private final int	blockSize;
		private final int	segmentCount;
		private final int	segmentSize;

		public SegmentLayout(final int segmentCount, final int segmentSize, final int blockSize) {
			this.segmentCount = segmentCount;
			this.segmentSize = segmentSize;
			this.blockSize = blockSize;
		}

		public int getBlockSize() {
			return this.blockSize;
		}

		public int getSegmentCount() {
			return this.segmentCount;
		}

		public int getSegmentSize() {
			return this.segmentSize;
		}
	}

	public final static boolean isPowerOfTwo(int v) {
		if (v <= 0) {
			return false;
		}
		return (v & (v - 1)) == 0;
	}

	/**
	 * 并发度，即segment数量
	 */
	private int					concurrency;
	private int					indexCapacity;

	/**
	 * 总table容量（每个segment容量=indexCapacity/concurrentLevel）
	 */
	private int					indexConflict;

	/**
	 * 分段锁密度，表示每隔对应个元素设置一个CAS锁
	 */
	private int					indexLockDensity;
	/**
	 * Key长度
	 */
	private int					keyLength;

	private List<SegmentLayout>	segmentLayoutList;

	public CacheLayout() {

	}

	public CacheLayout(List<SegmentLayout> segmentConfigurationList) {

		Preconditions.checkArgument(segmentConfigurationList.size() <= 256, "slab categories must be less than 256");
		Preconditions.checkArgument(segmentConfigurationList.size() > 0, "slab categories must be greater than 0");

		HashSet<Integer> check = new HashSet<Integer>();
		for (SegmentLayout segmentConfiguration : segmentConfigurationList) {
			int blockSize = segmentConfiguration.blockSize;

			Preconditions.checkArgument(segmentConfiguration.getSegmentCount() <= 256, "segmentCount must be less than 256");
			Preconditions.checkArgument(segmentConfiguration.getSegmentCount() > 0, "segmentCount must be greater than 0");

			if (!check.add(blockSize)) {
				throw new IllegalArgumentException("duplicated blockSize param, " + blockSize);
			}
		}
		this.segmentLayoutList = Collections.unmodifiableList(segmentConfigurationList);
	}

	public CacheLayout(List<SegmentLayout> segmentConfigurationList, int indexCapacity, int indexConflict, int concurrency, int indexLockDensity, int keyLength) {

		this.indexCapacity = indexCapacity;
		this.indexConflict = indexConflict;
		this.concurrency = concurrency;
		this.indexLockDensity = indexLockDensity;
		this.keyLength = keyLength;

		Preconditions.checkArgument(segmentConfigurationList.size() <= 256, "slab categories must be less than 256");
		Preconditions.checkArgument(segmentConfigurationList.size() > 0, "slab categories must be greater than 0");

		HashSet<Integer> check = new HashSet<Integer>();
		for (SegmentLayout segmentConfiguration : segmentConfigurationList) {
			int blockSize = segmentConfiguration.blockSize;

			Preconditions.checkArgument(segmentConfiguration.getSegmentCount() <= 256, "segmentCount must be less than 256");
			Preconditions.checkArgument(segmentConfiguration.getSegmentCount() > 0, "segmentCount must be greater than 0");

			if (!check.add(blockSize)) {
				throw new IllegalArgumentException("duplicated blockSize param, " + blockSize);
			}
		}
		this.segmentLayoutList = Collections.unmodifiableList(segmentConfigurationList);
	}

	@Override
	public void configure(JsonProperties property) {

		this.concurrency = property.getIntegerNotNull("concurrency");
		this.keyLength = property.getIntegerNotNull("index-key-length");
		this.indexLockDensity = property.getIntegerNotNull("index-lock-density");
		this.indexCapacity = property.getIntegerNotNull("index-capacity");
		this.indexConflict = property.getIntegerNotNull("index-conflict");

		if (!isPowerOfTwo(indexCapacity)) {
			throw new IllegalArgumentException("indexCapacity " + indexCapacity + " must be power of 2");
		}

		this.segmentLayoutList = new ArrayList<SegmentLayout>(16);

		List<JsonProperties> segmentJsonList = property.getArrayNotNull("segment-layouts");
		for (JsonProperties segmentJson : segmentJsonList) {
			int segmentCount = segmentJson.getIntegerNotNull("segment-count");
			int segmentSize = segmentJson.getIntegerNotNull("segment-size");

			int blockSize = segmentJson.getIntegerNotNull("block-size");

			SegmentLayout segmentConfiguration = new SegmentLayout(segmentCount, segmentSize, blockSize);
			this.segmentLayoutList.add(segmentConfiguration);
		}
	}

	public long evalSize() {
		if (this.segmentLayoutList == null) {
			return 0;
		}
		long sizeSum = 0;
		for (SegmentLayout c : this.segmentLayoutList) {
			sizeSum += c.getSegmentCount() * c.getSegmentSize();
		}
		return sizeSum;
	}

	public int getConcurrencyLevel() {
		return concurrency;
	}

	public int getIndexCapacity() {
		return indexCapacity;
	}

	public int getIndexConflict() {
		return indexConflict;
	}

	public int getIndexLockDensity() {
		return indexLockDensity;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public List<SegmentLayout> getSegmentConfigurationList() {
		return this.segmentLayoutList;
	}

	public List<SegmentLayout> getSegmentLayoutList() {
		return segmentLayoutList;
	}

	public int getSegmentTotalCount() {
		int count = 0;
		for (SegmentLayout segmentConfiguration : segmentLayoutList) {
			count += segmentConfiguration.getSegmentCount();
		}
		return count;
	}

	public void setConcurrentLevel(int concurrentLevel) {
		this.concurrency = concurrentLevel;
	}

	public void setIndexCapacity(int indexCapacity) {
		this.indexCapacity = indexCapacity;
	}

	public void setIndexConflict(int indexConflict) {
		this.indexConflict = indexConflict;
	}

	public void setIndexLockDensity(int lockDensity) {
		this.indexLockDensity = lockDensity;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public void setSegmentLayoutList(List<SegmentLayout> segmentLayoutList) {
		this.segmentLayoutList = segmentLayoutList;
	}

}
