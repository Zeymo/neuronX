package io.zeymo.neuron.io.codec;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;

import java.io.IOException;

public interface ObjectCodec {
	public Object decode(NeuronInputBuffer inputBuffer) throws IOException;

	public void encode(Object data, NeuronWriter writer) throws IOException;
}
