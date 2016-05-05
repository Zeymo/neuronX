package io.zeymo.neuron.io.codec.binary;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.domain.Varchar;
import io.zeymo.neuron.io.UnionBuffer;
import io.zeymo.neuron.io.codec.BinaryCodec;

import java.io.IOException;

public class BinaryStringCodec implements BinaryCodec {

	private final int			fieldIndex;

	private final UnionBuffer unionCache;

	public BinaryStringCodec(UnionBuffer unionCache, int fieldIndex) {
		this.unionCache = unionCache;
		this.fieldIndex = fieldIndex;
	}

	@Override
	public int decode(byte[] buffer, int offset) throws IOException {

		int dataLength = NeuronReader.readRawVarint32(buffer, offset);
		final int headLength = NeuronReader.measureVarint32(dataLength);

		unionCache.setSubInt(fieldIndex, 0, offset + headLength);
		unionCache.setSubInt(fieldIndex, 1, dataLength);

		return headLength + dataLength;
	}

	public int getDataLength() {
		return unionCache.getSubInt(fieldIndex, 1);
	}

	public int getDataOffset() {
		return unionCache.getSubInt(fieldIndex, 0);
	}

	@Override
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException {
		int length = inputBuffer.readShort();
		int position = inputBuffer.position();

		inputBuffer.skip(length);

		writer.writeUVInt(length);
		writer.writeBytes(inputBuffer.getBuffer(), position, length);
	}

	public static void encode(String value, NeuronWriter writer) throws IOException {
		final byte[] bytes = value.getBytes(NeuronConstants.STRING_CHARSET);
		writer.writeUVInt(bytes.length);
		writer.writeBytes(bytes);
	}

	public static void encode(Varchar value, NeuronWriter writer) throws IOException {
		final byte[] bytes = value.getBytes();
		final int length = value.getLength();
		writer.writeUVInt(length);
		writer.writeBytes(bytes, 0, length);
	}

	@Override
	public Object debug(byte[] buffer) {

		int off = unionCache.getSubInt(fieldIndex, 0);
		int len = unionCache.getSubInt(fieldIndex, 1);

		return new String(buffer, off, len, NeuronConstants.STRING_CHARSET);
	}

}
