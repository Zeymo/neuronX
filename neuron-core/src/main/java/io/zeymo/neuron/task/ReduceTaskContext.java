package io.zeymo.neuron.task;

import io.zeymo.neuron.runtime.NeuronCodecRuntime;
import io.zeymo.neuron.runtime.NeuronConfiguration;

public interface ReduceTaskContext {
	public NeuronCodecRuntime getCodecRuntime();

	public NeuronConfiguration getConfiguration();
}
