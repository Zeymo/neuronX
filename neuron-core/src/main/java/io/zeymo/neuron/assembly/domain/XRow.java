package io.zeymo.neuron.assembly.domain;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.neuron.NeuronConstants;

import java.io.IOException;

public class XRow {

	private byte[]	buffer	= new byte[NeuronConstants.RUNTIME_MAX_ROW_SIZE];
	private int		length;

	private boolean	delete;

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getLength() {
		return length;
	}

	public void set(byte[] buffer, int offset, int length) {
		this.length = length;
		System.arraycopy(buffer, offset, this.buffer, 0, length);
		this.delete = false;
	}

	public void set(NeuronInputBuffer inputBuffer) throws IOException {
		this.length = inputBuffer.readUVInt();
		inputBuffer.readBytes(buffer, 0, length);
		this.delete = false;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
