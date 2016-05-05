package io.zeymo.commons.io.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

public class   NeuronByteArrayReader extends NeuronBaseReader {

	private byte[]	buffer;

	protected int	limit;

	protected int	offset;

	protected int	position;

	public NeuronByteArrayReader() {

	}

	@Override
	public void close() {
		// DO NOTHING
	}

	public NeuronByteArrayReader(final byte[] buffer) {
		this.init(buffer, 0, buffer.length);
	}

	public NeuronByteArrayReader(final byte[] buffer, final int offset, final int length) {
		this.init(buffer, offset, length);
	}

	public int getRemaining() {
		return this.limit - this.position;
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

	@Override
	public long getRelativeOffset() {
		return this.position - this.offset;
	}

	public void init(final byte[] buffer) {
		this.buffer = buffer;
		this.position = 0;
		this.limit = buffer.length;
		this.offset = 0;
	}

	public void init(final byte[] buffer, final int offset, final int length) {
		this.buffer = buffer;
		this.position = offset;
		this.limit = length + offset;
		this.offset = offset;
	}

	@Override
	public byte readRawByte() throws IOException {
		if (this.position >= this.limit) {
			throw new IOException("position " + this.position + " out of bound " + this.limit);
		}
		return this.buffer[this.position++];
	}

	@Override
	public void readRawBytes(final byte[] dst, final int offset, final int length) throws IOException {
		if ((this.limit - this.position) >= length) {
			System.arraycopy(this.buffer, this.position, dst, offset, length);
			this.position += length;
		} else {
			throw new IOException("position " + this.position + " read length " + length + " out of bound " + this.limit);
		}
	}

	@Override
	public void readRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		if ((this.position + length) > this.limit) {
			throw new IOException("position " + this.position + "+ length " + length + " bytes out of bound " + this.limit);
		}
		byteBuffer.put(this.buffer, this.position, length);
		this.position += length;
	}

	public void setBuffer(final byte[] buffer) {
		this.buffer = buffer;
	}

	public void setLimit(final int limit) {
		this.limit = limit;
	}

	public void setOffset(final int offset) {
		this.offset = offset;
	}

	public void setPosition(final int position) {
		this.position = position;
	}

	@Override
	public void skip(final int bytes) throws IOException {
		if ((this.position + bytes) > this.limit) {
			throw new IOException("position " + this.position + "+ skip " + bytes + " bytes out of bound " + this.limit);
		}
		this.position += bytes;
	}

}
