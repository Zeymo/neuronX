package io.zeymo.neuron.task;

import io.zeymo.commons.io.NeuronInputBuffer;

import java.io.IOException;

public interface Mapper {

	public void init(MapTaskContext taskContext);

	public void initMapTask(NeuronInputBuffer contextInput) throws IOException;

	public void map(long nodeId, MapTaskContext taskContext, TaskOutputCollector collector) throws IOException;

	public void finishMapTask(TaskOutputCollector output) throws IOException;
}
