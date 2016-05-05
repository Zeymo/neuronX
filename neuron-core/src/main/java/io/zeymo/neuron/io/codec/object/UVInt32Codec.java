package io.zeymo.neuron.io.codec.object;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.codec.ObjectCodec;

import java.io.IOException;

public class UVInt32Codec implements ObjectCodec {

	@Override
	public Object decode(NeuronInputBuffer inputBuffer) throws IOException {
		return inputBuffer.readUVInt();
	}

	@Override
	public void encode(Object data, NeuronWriter writer) throws IOException {
		Number value = (Number) data;
		writer.writeUVInt(value.intValue());
	}
}
