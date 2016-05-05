package io.zeymo.commons.io;


import io.zeymo.commons.io.impl.NeuronByteArrayReader;

import java.io.IOException;
import java.nio.BufferOverflowException;

public class NeuronInputBuffer extends NeuronByteArrayReader implements BinaryWritable {
	private byte[]	buffer;
	private int		capacity;

	public NeuronInputBuffer() {

	}
	
	public void reset(byte[] buffer, int offset, int capacity) {
		this.init(buffer, offset, capacity);
	}

	public void reset(byte[] buffer, int capacity) {
		this.init(buffer, 0, capacity);
	}

	public NeuronInputBuffer(int capacity) {
		this.buffer = new byte[capacity];
		this.capacity = capacity;
		super.init(buffer);
	}

	public NeuronInputBuffer(byte[] buffer) {
		this.buffer = buffer;
		this.capacity = buffer.length;
		super.init(buffer);
	}

	public void clear() {
		this.init(buffer, 0, 0);
		this.limit = 0;
	}

	@Override
	public byte[] getBuffer() {
		return buffer;
	}

	public int getCapacity() {
		return capacity;
	}

	@Override
	public void init(final byte[] buffer, final int offset, final int length) {
		super.init(buffer, offset, length);
	}

	public void limit(int limit) {
		this.limit = limit;
	}

	public int position() {
		return this.position;
	}

	public boolean hasNext(int limit) {
		return this.position < limit;
	}

	public boolean hasNext() {
		return this.position < this.limit;
	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {
		int length = reader.readUVInt();
		this.position = reader.readUVInt();
		if (length > buffer.length) {
			throw new BufferOverflowException();
		}
		reader.readBytes(buffer, 0, length);
		this.init(buffer, 0, length);
		this.limit = length;

	}

	public void reset() {
		this.init(buffer, 0, limit);
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		writer.writeUVInt(limit);
		writer.writeUVInt(position);
		writer.writeBytes(buffer, 0, limit);
	}

}
