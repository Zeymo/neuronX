package io.zeymo.neuron.io.codec.object;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.codec.ObjectCodec;

import java.io.IOException;

public class Int8Codec implements ObjectCodec {
	@Override
	public Object decode(NeuronInputBuffer inputBuffer) throws IOException {
		return (int) inputBuffer.readByte();
	}

	@Override
	public void encode(Object data, NeuronWriter writer) throws IOException {
		Number value = (Number) data;
		writer.writeByte(value.intValue());
	}
}
