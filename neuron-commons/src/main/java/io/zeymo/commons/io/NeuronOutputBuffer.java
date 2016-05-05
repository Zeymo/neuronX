package io.zeymo.commons.io;


import io.zeymo.commons.io.impl.NeuronByteArrayWriter;

import java.util.Arrays;

public class NeuronOutputBuffer extends NeuronByteArrayWriter {

	public NeuronOutputBuffer(int initialCapacity) {
		super(new byte[initialCapacity]);
	}

	public NeuronOutputBuffer(byte[] buffer) {
		super(buffer);
	}

	public NeuronOutputBuffer() {
		this(4096);
	}

	public void reset() {
		super.reset(buffer);
	}

	@Override
	public void reset(byte[] buffer) {
		super.reset(buffer);
	}

	public void reset(byte[] buffer, int length) {
		super.init(buffer, 0, length);
	}

	@Override
	public void grow(int minCapacity) {

		int oldCapacity = buffer.length;
		int newCapacity = oldCapacity << 1;
		if (newCapacity - minCapacity < 0)
			newCapacity = minCapacity;
		if (newCapacity < 0) {
			if (minCapacity < 0)
				throw new OutOfMemoryError();
			newCapacity = Integer.MAX_VALUE;
		}

		buffer = Arrays.copyOf(buffer, newCapacity);
		this.limit = newCapacity;
	}

}
