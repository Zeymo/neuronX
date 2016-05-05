package io.zeymo.commons.io.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

public class NeuronStreamWriter extends NeuronBaseWriter {

	private final OutputStream	output;
	private long				relativeOffset;

	public NeuronStreamWriter(final OutputStream output) {
		this.output = output;
	}

	public NeuronStreamWriter(final OutputStream output, long initialOffset) {
		this.output = output;
		this.relativeOffset = initialOffset;
	}

	@Override
	public void flush() throws IOException {
		if (this.output != null) {
			this.output.flush();
		}
	}

	@Override
	public void writeRawByte(final int b) throws IOException {
		this.output.write(b);
		++relativeOffset;
	}

	@Override
	public void writeRawBytes(final byte[] src, final int offset, final int length) throws IOException {
		this.output.write(src, offset, length);
		relativeOffset += length;
	}

	@Override
	public void writeRawBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		for (int i = 0; i < length; ++i) {
			this.output.write(byteBuffer.get());
		}
		relativeOffset += length;
	}

	@Override
	public void close() throws IOException {
		flush();
		this.output.close();
	}

	@Override
	public int getRelativeOffset() {
		return (int) relativeOffset;
	}

	public long getLongRelativeOffset() {
		return relativeOffset;
	}

}
