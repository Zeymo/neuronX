package io.zeymo.neuron.task;

import io.zeymo.neuron.runtime.NeuronCodecRuntime;
import io.zeymo.neuron.runtime.NeuronConfiguration;

import java.io.IOException;

public interface MapTaskContext {

	public boolean getNode(long nodeId) throws IOException;

	public NeuronCodecRuntime getCodecRuntime();

	public NeuronConfiguration getConfiguration();
}
