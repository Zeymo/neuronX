package io.zeymo.commons.io.impl;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class NeuronFileStreamWriter extends NeuronBaseWriter {

	private final FileOutputStream		fileOutput;
	private final BufferedOutputStream	output;
	private long						relativeOffset;

	public NeuronFileStreamWriter(final FileOutputStream output, final int bufferSize) {
		this.output = new BufferedOutputStream(output, bufferSize);
		this.fileOutput = output;
	}

	public NeuronFileStreamWriter(final FileOutputStream output) {
		this(output, 8192);
	}

	public NeuronFileStreamWriter(final FileOutputStream output, long initialOffset) {
		this(output, 8192);
		this.relativeOffset = initialOffset;
	}

	@Override
	public void flush() throws IOException {
		if (this.output != null) {
			this.output.flush();
			this.fileOutput.getFD().sync();
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
