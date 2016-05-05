package io.zeymo.neuron.io.codec.binary;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.io.codec.IntDecoder;

import java.io.IOException;

public class Int32Codec implements BinaryCodec, IntDecoder {

	private final int			fieldIndex;
	private final UnionBuffer unionCache;

	public Int32Codec(UnionBuffer unionCache, int fieldIndex) {
		this.unionCache = unionCache;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public int decode(byte[] buffer, int offset) throws IOException {
		int value = NeuronReader.readRawInt32(buffer, offset);
		unionCache.setInt(fieldIndex, value);
		return NeuronReader.INT_SIZE;
	}

	@Override
	public int getValue() {
		return unionCache.getInt(fieldIndex);
	}

	@Override
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException {
		int value = inputBuffer.readFInt();
		writer.writeFInt(value);
	}

	public static void encode(int value, NeuronWriter writer) throws IOException {
		writer.writeFInt(value);
	}

	@Override
	public Object debug(byte[] buffer) {
		int value = unionCache.getInt(fieldIndex);
		return value;
	}

}
