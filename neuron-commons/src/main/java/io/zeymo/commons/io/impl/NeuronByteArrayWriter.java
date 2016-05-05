package io.zeymo.commons.io.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NeuronByteArrayWriter extends NeuronBaseWriter {

	protected byte[]	buffer;
	protected int		limit;
	protected int		offset;
	protected int		position;

	public NeuronByteArrayWriter() {

	}

	public NeuronByteArrayWriter(final byte[] buffer) {
		this(buffer, 0, buffer.length);
	}

	public NeuronByteArrayWriter(final byte[] buffer, final int offset, final int length) {
		this.init(buffer, offset, length);
	}

	public void close() {
		// DO NOTHING
	}

	@Override
	public void flush() {
		// DO NOTHING
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public int getLimit() {
		return this.limit;
	}

	public int getOffset() {
		return this.offset;
	}

	public int getPosition() {
		return this.position;
	}

	public int getRelativeOffset() {
		return position - offset;
	}

	public void grow(int minCapacity) throws IOException {
		throw new IOException("position " + this.position + " out of bound " + this.limit);
	}

	public void reset(final byte[] buffer) {
		this.init(buffer, 0, buffer.length);
	}

	public void init(final byte[] buffer, final int offset, final int length) {
		this.buffer = buffer;
		this.position = offset;
		this.offset = offset;
		this.limit = length + offset;
	}

	public void position(final int position) {
		this.position = position;
	}

	@Override
	public void writeRawByte(final int b) throws IOException {
		while (this.position >= this.limit) {
			this.grow(this.limit + 1);
		}
		this.buffer[this.position++] = (byte) b;
	}

	@Override
	public void writeRawBytes(final byte[] src, final int offset, final int length) throws IOException {
		if (this.position + length > this.limit) {
			this.grow(this.position + length);
		}

		System.arraycopy(src, offset, this.buffer, this.position, length);
		this.position += length;

	}

	@Override
	public void writeRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {

		if (this.position + length > this.limit) {
			this.grow(this.position + length);
		}

		byteBuffer.get(this.buffer, this.position, length);
		this.position += length;
	}

	public void writeRawBytes(final int b, final int repeat) throws IOException {
		if (this.position + repeat > this.limit) {
			this.grow(this.position + repeat);
		}

		for (int i = 0; i < repeat; ++i) {
			this.buffer[this.position++] = (byte) b;
		}
	}
}
