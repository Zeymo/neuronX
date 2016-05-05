package io.zeymo.neuron.io;

import io.zeymo.neuron.NeuronConstants;

public class UnionBufferDebugger {
	private byte[]		buffer;

	private UnionBuffer	unionBuffer;

	public UnionBufferDebugger(UnionBuffer unionBuffer, byte[] buffer) {
		this.unionBuffer = unionBuffer;
		this.buffer = buffer;
	}

	public long getLong(int fieldIndex) {
		return unionBuffer.getLong(fieldIndex);
	}

	public double getDouble(int fieldIndex) {
		return unionBuffer.getDouble(fieldIndex);
	}

	public int getInt(int fieldIndex) {
		return unionBuffer.getInt(fieldIndex);
	}

	public String getString(int fieldIndex) {
		int offset = unionBuffer.getSubInt(fieldIndex, 0);
		int length = unionBuffer.getSubInt(fieldIndex, 1);
		return new String(buffer, offset, length, NeuronConstants.STRING_CHARSET);
	}
}
