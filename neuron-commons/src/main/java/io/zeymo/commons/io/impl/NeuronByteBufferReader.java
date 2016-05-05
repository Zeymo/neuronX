package io.zeymo.commons.io.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NeuronByteBufferReader extends NeuronBaseReader {

	private ByteBuffer	buffer;
	private int			offset;

	public void reset(ByteBuffer buffer) {
		this.init(buffer);
	}

	public NeuronByteBufferReader() {

	}

	public NeuronByteBufferReader(final ByteBuffer buffer) {
		this.init(buffer);
	}

	@Override
	public void close() {
		// DO NOTHING
	}

	@Override
	public long getRelativeOffset() {
		// NO FLIP/CLEAR/RESET ops BEFORE THIS CALL!!
		return this.buffer.position() - this.offset;
	}

	public void init(final ByteBuffer buffer) {
		this.buffer = buffer;
		this.offset = this.buffer.position();
	}

	@Override
	public byte readRawByte() throws IOException {
		return this.buffer.get();
	}

	@Override
	public void readRawBytes(final byte[] dst, final int offset, final int length) throws IOException {
		this.buffer.get(dst, offset, length);
	}

	@Override
	public void readRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		for (int i = 0; i < length; ++i) {
			byteBuffer.put(this.buffer.get());
		}
	}

	@Override
	public void skip(final int bytes) throws IOException {
		if (bytes > this.buffer.remaining()) {
			throw new IOException("remaining " + this.buffer.remaining() + "+ skip " + bytes + " out of buffer");
		}
		this.buffer.position(this.buffer.position() + bytes);
	}

}
