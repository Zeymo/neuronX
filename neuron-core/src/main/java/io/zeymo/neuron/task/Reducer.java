package io.zeymo.neuron.task;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronOutputBuffer;

import java.io.IOException;

public interface Reducer {

	public void init(ReduceTaskContext taskContext);

	public void initReduceTask(NeuronInputBuffer contextInput) throws IOException;

	public void reduce(NeuronInputBuffer input) throws IOException;

	public void finishReduceTask(NeuronOutputBuffer output) throws IOException;

}
