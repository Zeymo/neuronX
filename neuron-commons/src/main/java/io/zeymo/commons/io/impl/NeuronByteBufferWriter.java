package io.zeymo.commons.io.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NeuronByteBufferWriter extends NeuronBaseWriter {

	private ByteBuffer	buffer;
	private int			relativeOffset;

	public NeuronByteBufferWriter() {

	}

	public void reset(final ByteBuffer buffer) {
		this.buffer = buffer;
		this.relativeOffset = 0;
	}

	public NeuronByteBufferWriter(final ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public void close() {
		// DO NOTHING
	}

	@Override
	public void flush() throws IOException {
		// NOTHING TO DO
	}

	@Override
	public void writeRawByte(final int b) throws IOException {
		this.buffer.put((byte) b);
		++relativeOffset;

	}

	@Override
	public void writeRawBytes(final byte[] src, final int offset, final int length) throws IOException {
		this.buffer.put(src, offset, length);
		relativeOffset += length;
	}

	@Override
	public void writeRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		for (int i = 0; i < length; ++i) {
			this.buffer.put(byteBuffer.get());
		}
		relativeOffset += length;
	}

	@Override
	public int getRelativeOffset() {
		return relativeOffset;
	}
}
