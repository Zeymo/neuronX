package io.zeymo.neuron.domain;

import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.neuron.NeuronAnnotations;

import java.io.IOException;

@NeuronAnnotations.GCFree
public class Sector {

	private int		typeIndex;
	private int		count;

	private byte[]	buffer;
	private int		dataOffset;
	private int		dataLength;

	// private int allocLength;

	// public int getAllocLength() {
	// return allocLength;
	// }
	//
	// public void setAllocLength(int allocLength) {
	// this.allocLength = allocLength;
	// }

	// @Override
	public int reset(NeuronByteArrayReader reader, byte[] buffer, int offset, int length) throws IOException {
		NeuronReader.reuse(reader, buffer, offset, length);
		this.buffer = buffer;

		this.typeIndex = reader.readByte();
		this.count = reader.readUVInt();
		// this.allocLength = reader.readFInt();
		this.dataLength = reader.readUVInt();

		int relativeOffset = (int) reader.getRelativeOffset();

		// beginning of data
		this.dataOffset = offset + relativeOffset;

		// total bytes in this sector
		return relativeOffset + this.dataLength;
	}

	public int getTypeIndex() {
		return typeIndex;
	}

	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public int getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

}
