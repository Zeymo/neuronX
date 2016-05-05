package io.zeymo.neuron.task;

import io.zeymo.commons.io.BinaryWritable;

import java.io.IOException;

public interface TaskOutputCollector {
	public void collect(BinaryWritable value) throws IOException;
}
