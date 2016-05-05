package io.zeymo.neuron.io.codec.binary;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.io.codec.LongDecoder;

import java.io.IOException;

public class Int64Codec implements BinaryCodec, LongDecoder {

	private final int			fieldIndex;
	private final UnionBuffer unionCache;

	public Int64Codec(UnionBuffer unionCache, int fieldIndex) {
		this.unionCache = unionCache;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public int decode(byte[] buffer, int offset) throws IOException {
		long value = NeuronReader.readRawInt64(buffer, offset);
		unionCache.setLong(fieldIndex, value);

		return NeuronReader.LONG_SIZE;
	}

	@Override
	public long getValue() {
		return unionCache.getLong(fieldIndex);
	}

	@Override
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException {
		long value = inputBuffer.readFLong();
		writer.writeFLong(value);
	}

	public static void encode(long value, NeuronWriter writer) throws IOException {
		writer.writeFLong(value);
	}

	@Override
	public Object debug(byte[] buffer) {
		long value = unionCache.getLong(fieldIndex);
		return value;
	}

}
