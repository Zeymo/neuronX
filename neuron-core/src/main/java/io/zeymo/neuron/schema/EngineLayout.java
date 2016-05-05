package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConstructable;

public class EngineLayout implements JsonPropertyConstructable {

	private final int		engineWorkerCount;
	private final int		eventPoolSize;
	private final int		eventPoolCount;
	private final boolean	logCleanEnabled;
	private final boolean	logRetireEnabled;
	private final int		logRetireInterval;
	private final long		logTaskTimeoutMs;

	public EngineLayout(JsonProperties property) {
		this.logCleanEnabled = property.getBoolean("log-clean-enabled", false);
		this.logRetireEnabled = property.getBoolean("log-retire-enabled", false);
		this.logRetireInterval = property.getInteger("log-retire-interval", 86400);
		this.eventPoolSize = property.getIntegerNotNull("event-queue-size");
		this.eventPoolCount = property.getIntegerNotNull("event-pool-count");
		this.engineWorkerCount = property.getIntegerNotNull("engine-worker-count");
		this.logTaskTimeoutMs = property.getInteger("log-task-timeout", 1000);
	}

	public int getEngineWorkerCount() {
		return engineWorkerCount;
	}

	public int getEventPoolSize() {
		return eventPoolSize;
	}

	public int getEventPoolCount() {
		return eventPoolCount;
	}

	public int getLogRetireInterval() {
		return logRetireInterval;
	}

	public long getLogTaskTimeoutMs() {
		return logTaskTimeoutMs;
	}

	public boolean isLogCleanEnabled() {
		return logCleanEnabled;
	}

	public boolean isLogRetireEnabled() {
		return logRetireEnabled;
	}
}
