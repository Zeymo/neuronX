package io.zeymo.neuron.io.codec.object;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.io.codec.ObjectCodec;

import java.io.IOException;

public class BinaryStringCodec implements ObjectCodec {
	@Override
	public Object decode(NeuronInputBuffer inputBuffer) throws IOException {
		int length = inputBuffer.readUVInt();
		byte[] bytes = new byte[length];

		inputBuffer.readBytes(bytes);
		return new String(bytes, NeuronConstants.STRING_CHARSET);
	}

	@Override
	public void encode(Object data, NeuronWriter writer) throws IOException {
		String value = data.toString();
		BinaryStringCodec.encode(value, writer);
	}

	public static void encode(String value, NeuronWriter writer) throws IOException {
		final byte[] bytes = value.getBytes(NeuronConstants.STRING_CHARSET);
		writer.writeUVInt(bytes.length);
		writer.writeBytes(bytes);
	}

}
