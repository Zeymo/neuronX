package io.zeymo.neuron.io.codec.binary;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;
import io.zeymo.neuron.io.codec.LongDecoder;

import java.io.IOException;

public class SVInt64Codec implements BinaryCodec, LongDecoder {

	private final int			fieldIndex;
	private final UnionBuffer unionCache;

	public SVInt64Codec(UnionBuffer unionCache, int fieldIndex) {
		this.unionCache = unionCache;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public int decode(byte[] buffer, int offset) throws IOException {
		int start = offset;

		int shift = 0;
		long result = 0;

		while (shift < 64) {
			final byte b = buffer[offset++];
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				break;
			}
			shift += 7;
		}

		unionCache.setLong(fieldIndex, NeuronReader.uncrossInt64(result));
		return offset - start;

	}

	@Override
	public long getValue() {
		return unionCache.getLong(fieldIndex);
	}

	@Override
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException {
		long bits = inputBuffer.readSVLong();
		writer.writeSVLong(bits);
	}

	public static void encode(long value, NeuronWriter writer) throws IOException {
		writer.writeSVLong(value);
	}

	@Override
	public Object debug(byte[] buffer) {
		long value = unionCache.getLong(fieldIndex);
		return value;
	}

}
