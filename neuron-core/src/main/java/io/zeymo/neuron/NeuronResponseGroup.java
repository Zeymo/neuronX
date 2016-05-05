package io.zeymo.neuron;

import io.zeymo.commons.annotation.NotThreadSafe;
import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.commons.io.impl.NeuronByteArrayWriter;
import io.zeymo.neuron.domain.Varchar;

import java.io.IOException;

@NotThreadSafe
public class NeuronResponseGroup implements BinaryWritable {

	public static class ValueIterator<T extends BinaryWritable> {
		private final NeuronResponseGroup			group;
		private final NeuronResponse.ResponseHeader	header;
		private final NeuronInputBuffer input;
		private int									sequence;
		private final int							sequenceLimit;
		private final T								value;
		private final boolean						raiseException;

		public ValueIterator(NeuronResponseGroup responseGroup, T value, boolean raiseException) throws IOException {
			this.group = responseGroup;
			this.input = new NeuronInputBuffer();
			this.header = new NeuronResponse.ResponseHeader();
			this.sequence = 0;
			this.sequenceLimit = group.getGroupCount();
			this.value = value;
			this.raiseException = raiseException;
			sequence = -1;
			this.nextInput();
		}

		public boolean hasNext() throws IOException {
			while (true) {
				if (this.input.hasNext()) {
					return true;
				}
				if (!this.nextInput()) {
					return false;
				}
			}
		}

		public T next() throws IOException {
			while (true) {
				if (this.input.hasNext()) {
					value.readFields(input);
					return value;
				}
				if (!this.nextInput()) {
					return null;
				}
			}
		}

		private boolean nextInput() throws IOException {
			while (++sequence < sequenceLimit) {

				int offset = group.getOffset(sequence);
				int length = group.getLength(sequence);

				// 跳过没有内容的Input
				if (length <= NeuronResponse.ResponseHeader.LENGTH) {
					continue;
				}

				input.init(group.getBuffer(), offset, length);
				header.readFields(input);

				// 只要状态OK的返回值
				if (header.getCode() == NeuronResponseCode.OK) {
					return true;
				} else if (raiseException) {
					int messageLen = header.getBodyLength();
					byte[] messageBuffer = new byte[messageLen];
					input.readBytes(messageBuffer);
					throw new RuntimeException(new String(messageBuffer, NeuronConstants.STRING_CHARSET));
				}

			}
			return false;

		}

	}

	public static <T extends BinaryWritable> ValueIterator<T> iteratorWithException(NeuronResponseGroup responseGroup, T value) throws IOException {
		return new ValueIterator<T>(responseGroup, value, true);
	}

	public static <T extends BinaryWritable> ValueIterator<T> iterator(NeuronResponseGroup responseGroup, T value) throws IOException {
		return new ValueIterator<T>(responseGroup, value, false);
	}

	private final byte[]				buffer;

	// public static void writeSingle(NeuronWriter writer, byte[] buffer, int offset, int length) throws IOException {
	// writer.writeUVInt(1);
	// writer.writeUVInt(length); // length[0]
	// writer.writeUVInt(length); // bufferLength
	// writer.writeBytes(buffer, offset, length);
	// }

	private volatile int				bufferLength;

	private final int					capacity;
	private volatile int				groupCount;

	private final int					groupLimit;

	private final int[]					lengths;
	private final int[]					offsets;

	private final NeuronByteArrayReader	reader;

	private final NeuronByteArrayWriter	writer;

	public NeuronResponseGroup() {
		this(NeuronConstants.RUNTIME_RPC_MAX_RESPONSE_GROUP_CAPACITY, NeuronConstants.RUNTIME_RPC_MAX_RESPONSE_GROUP_SUB_COUNT);
	}

	public NeuronResponseGroup(int capacity, int batchLimit) {
		this.capacity = capacity;
		this.groupLimit = batchLimit;

		this.buffer = new byte[capacity];
		this.offsets = new int[batchLimit];
		this.lengths = new int[batchLimit];

		this.writer = new NeuronByteArrayWriter(this.buffer);
		this.reader = new NeuronByteArrayReader(this.buffer);
	}

	public void get(int index, NeuronResponse response) throws IOException {
		int offset = this.offsets[index];
		int length = this.lengths[index];

		this.reader.init(buffer, offset, length);
		response.readFields(reader);
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getBufferLength() {
		return bufferLength;
	}

	public int getGroupCount() {
		return groupCount;
	}

	public int getGroupLimit() {
		return groupLimit;
	}

	public int getLength(int sequence) {
		return lengths[sequence];
	}

	public int getOffset(int sequence) {
		return offsets[sequence];
	}

	public void put(NeuronResponse response) throws IOException {

		this.writer.init(buffer, bufferLength, buffer.length - bufferLength);
		response.write(writer);

		offsets[groupCount] = bufferLength;
		lengths[groupCount] = writer.getPosition() - bufferLength;

		this.bufferLength = writer.getPosition();
		this.groupCount++;
	}

	public boolean putError(int machineId, Varchar message) throws IOException {
		final int length = message.getLength();
		final int estimateLength = NeuronResponse.ResponseHeader.LENGTH + message.getLength();

		if (this.bufferLength + estimateLength > capacity) {
			return false;
		}

		writer.init(buffer, bufferLength, estimateLength);
		NeuronResponse.protocolWriteHeader(writer, machineId, NeuronResponseCode.ERROR, length);
		writer.writeBytes(message.getBytes(), 0, length);

		offsets[groupCount] = bufferLength;
		lengths[groupCount] = writer.getRelativeOffset();
		bufferLength += estimateLength;
		groupCount++;
		return true;
	}

	public boolean putRawResponse(byte[] bytes, int offset, int length) {
		if (this.bufferLength + length > capacity) {
			return false;
		}

		System.arraycopy(bytes, offset, buffer, bufferLength, length);
		offsets[groupCount] = bufferLength;
		lengths[groupCount] = length;
		bufferLength += length;
		groupCount++;
		return true;
	}

	public boolean putResponse(int machineId, int code, byte[] bytes, int offset, int length) throws IOException {
		final int estimateLength = NeuronResponse.ResponseHeader.LENGTH + length;

		if (this.bufferLength + estimateLength > capacity) {
			return false;
		}

		writer.init(buffer, bufferLength, estimateLength);
		NeuronResponse.protocolWriteHeader(writer, machineId, code, length);
		writer.writeBytes(bytes, offset, length);

		offsets[groupCount] = bufferLength;
		lengths[groupCount] = writer.getRelativeOffset();
		bufferLength += estimateLength;
		groupCount++;
		return true;
	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {
		this.groupCount = reader.readUVInt();

		int offset = 0;
		for (int i = 0; i < this.groupCount; ++i) {
			offsets[i] = offset;
			int length = reader.readUVInt();
			lengths[i] = length;
			offset += length;
		}

		this.bufferLength = reader.readUVInt();
		reader.readBytes(buffer, 0, bufferLength);
	}

	public void reset() {
		this.groupCount = 0;
		this.bufferLength = 0;
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		writer.writeUVInt(groupCount);
		for (int i = 0; i < this.groupCount; ++i) {
			int length = lengths[i];
			writer.writeUVInt(length);
		}
		writer.writeUVInt(bufferLength);
		writer.writeBytes(buffer, 0, bufferLength);

	}

}
