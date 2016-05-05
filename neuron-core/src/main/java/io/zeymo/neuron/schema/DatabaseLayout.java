package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;
import io.zeymo.commons.properties.JsonPropertyUtils;

public class DatabaseLayout implements JsonPropertyConfigurable {

	// public int getCacheIndexCapacity() {
	// return cacheIndexCapacity;
	// }
	//
	// public void setCacheIndexCapacity(int cacheIndexCapacity) {
	// this.cacheIndexCapacity = cacheIndexCapacity;
	// }
	//
	// public int getCacheIndexConflict() {
	// return cacheIndexConflict;
	// }
	//
	// public void setCacheIndexConflict(int cacheIndexConflict) {
	// this.cacheIndexConflict = cacheIndexConflict;
	// }
	//
	// public int getCacheIndexLockDensity() {
	// return cacheIndexLockDensity;
	// }
	//
	// public void setCacheIndexLockDensity(int cacheIndexLockDensity) {
	// this.cacheIndexLockDensity = cacheIndexLockDensity;
	// }

	// private int cacheIndexCapacity;
	// private int cacheIndexConflict;
	// private int cacheIndexLockDensity;
	private AllocPolicyLayout	allocPolicyLayout;
	private int					concurrency;
	private String				dataDirectory;
	private int					dirtyPageCapacity;
	private int					fileIndexCapacity;
	private int					fileIndexConflict;
	private int					fileIndexLockDensity;
	private boolean				lazyPersist;

	private int					logBatchSize;
	private String				logDirectory;

	private JsonProperties properties;

	@Override
	public void configure(JsonProperties property) {
		this.properties = property;
		this.dirtyPageCapacity = property.getIntegerNotNull("dirty-page-capacity");
		this.dataDirectory = property.getStringNotNull("data-directory");
		this.logDirectory = property.getStringNotNull("log-directory");
		this.concurrency = property.getIntegerNotNull("concurrency");
		this.logBatchSize = property.getIntegerNotNull("log-batch-size");
		this.fileIndexConflict = property.getIntegerNotNull("file-index-conflict");
		this.fileIndexCapacity = property.getIntegerNotNull("file-index-capacity");
		this.fileIndexLockDensity = property.getIntegerNotNull("file-index-lock-density");
		this.lazyPersist = property.getBooleanNotNull("lazy-persist");

		// this.cacheIndexCapacity =
		// property.getIntegerNotNull("cache-index-capacity");
		// this.cacheIndexConflict =
		// property.getIntegerNotNull("cache-index-conflict");
		// this.cacheIndexLockDensity =
		// property.getIntegerNotNull("cache-index-lock-density");
		this.allocPolicyLayout = JsonPropertyUtils.newInstance(AllocPolicyLayout.class, property.getSubProperties("alloc-policy-layout"));

	}

	public AllocPolicyLayout getAllocPolicyLayout() {
		return allocPolicyLayout;
	}

	public int getConcurrency() {
		return concurrency;
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public int getDirtyPageCapacity() {
		return dirtyPageCapacity;
	}

	public int getFileIndexCapacity() {
		return fileIndexCapacity;
	}

	public int getFileIndexConflict() {
		return fileIndexConflict;
	}

	public int getFileIndexLockDensity() {
		return fileIndexLockDensity;
	}

	public int getLogBatchSize() {
		return logBatchSize;
	}

	public String getLogDirectory() {
		return logDirectory;
	}

	public boolean isLazyPersist() {
		return lazyPersist;
	}

	public void setAllocPolicyLayout(AllocPolicyLayout allocPolicyLayout) {
		this.allocPolicyLayout = allocPolicyLayout;
	}

	public void setConcurrency(int concurrency) {
		this.concurrency = concurrency;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public void setDirtyPageCapacity(int dirtyPageCapacity) {
		this.dirtyPageCapacity = dirtyPageCapacity;
	}

	public void setFileIndexCapacity(int fileIndexCapacity) {
		this.fileIndexCapacity = fileIndexCapacity;
	}

	public void setFileIndexConflict(int fileIndexConflict) {
		this.fileIndexConflict = fileIndexConflict;
	}

	public void setFileIndexLockDensity(int fileIndexLockDensity) {
		this.fileIndexLockDensity = fileIndexLockDensity;
	}

	public void setLazyPersist(boolean lazyPersist) {
		this.lazyPersist = lazyPersist;
	}

	public void setLogDirectory(String logDirectory) {
		this.logDirectory = logDirectory;
	}

	@Override
	public String toString() {
		return properties.toPrettyString();
	}

}
