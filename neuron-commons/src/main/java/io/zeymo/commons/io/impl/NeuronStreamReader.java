package io.zeymo.commons.io.impl;

import org.apache.commons.io.IOUtils;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * 由于没有友好的机制判断输入流是否达到末尾，需要在外围代码判断EOFException 或者使用 getRelativeOffset + streamLength 判断
 */
public class NeuronStreamReader extends NeuronBaseReader {

	private InputStream	input;
	private long		bytesRead;

	public NeuronStreamReader(final InputStream input) {
		this.init(input);
	}

	public long getRelativeOffset() {
		return this.bytesRead;
	}

	public void init(final InputStream input) {
		this.input = input;
		this.bytesRead = 0;
	}

	@Override
	public byte readRawByte() throws IOException {
		++this.bytesRead;
		final int val = this.input.read();
		if (val == -1) {
			throw new EOFException();
		}
		return (byte) val;
	}

	@Override
	public void readRawBytes(final byte[] dst, final int offset, final int length) throws IOException {
		IOUtils.readFully(input, dst, offset, length);
		// INCR bytesRead after a successful reading, to find where is the
		// damaged data.
		this.bytesRead += length;

	}

	@Override
	public void readRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		for (int i = 0; i < length; ++i) {
			byteBuffer.put((byte) this.input.read());
		}
		// INCR bytesRead after a successful reading, to find where is the
		// damaged data.
		this.bytesRead += length;
	}

	public void skip(final int bytes) throws IOException {
		this.input.skip(bytes);
	}

	public void close() throws IOException {
		this.input.close();
	}

}
