package io.zeymo.neuron.domain;

import io.zeymo.neuron.NeuronAnnotations;
import io.zeymo.neuron.io.codec.BinaryCodec;

import java.io.IOException;

@NeuronAnnotations.GCFree
public class SectorIterator {
	private byte[]			buffer;

	private int				rowLimit;
	private int				rowNumber;
	private int				currentOffset;
	private int				currentLength;
	private BinaryCodec[]	decoderArray;
	private int				offsetLimit;

	public SectorIterator() {

	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getRowLimit() {
		return rowLimit;
	}

	/**
	 * 当前行row的行号，从0开始
	 * @return
	 */
	public int getRowNumber() {
		return rowNumber - 1;
	}

	public int getCurrentOffset() {
		return currentOffset;
	}

	@SuppressWarnings("unchecked")
	public <T extends BinaryCodec> T getDecoder(int index) {
		return (T) decoderArray[index];
	}

	public BinaryCodec[] getCodecArray() {
		return decoderArray;
	}

	public int getOffsetLimit() {
		return offsetLimit;
	}

	public boolean hasNext() {
		return rowNumber < rowLimit;
	}

	public void loadExternal(byte[] buffer, int offset, int offsetLimit) throws IOException {

		int colCount = decoderArray.length;

		for (int i = 0; i < colCount; ++i) {
			BinaryCodec decoder = decoderArray[i];
			offset += decoder.decode(buffer, offset);
		}

		if (offset > offsetLimit) {
			throw new IOException("decoder overflow," + currentOffset + "," + offset + "," + offsetLimit + "," + rowNumber + "," + rowLimit);
		}
	}

	public boolean next() throws IOException {
		if (!hasNext()) {
			return false;
		}

		final int colCount = decoderArray.length;

		int offset = currentOffset + currentLength;
		currentOffset = offset;

		// int offset = currentOffset;

		for (int i = 0; i < colCount; ++i) {
			BinaryCodec decoder = decoderArray[i];
			offset += decoder.decode(buffer, offset);
		}

		if (offset > offsetLimit) {
			throw new IOException("decoder overflow, curOff = " + currentOffset + ", off = " + offset + ", limit = " + offsetLimit + ", row = " + (rowNumber - 1) + ", rowLimit = " + rowLimit);
		}

		currentLength = offset - currentOffset;
		// currentLength = offset - currentLength;
		// currentLength = offset - currentLength;
		// currentOffset = offset;
		++rowNumber;

		return true;
	}

	public int getCurrentLength() {
		return currentLength;
	}

	public void reset(byte[] buffer, Sector sector) {
		this.buffer = buffer;
		this.rowNumber = 0;
		this.rowLimit = sector.getCount();
		this.currentOffset = sector.getDataOffset();
		this.currentLength = 0;
		this.offsetLimit = sector.getDataOffset() + sector.getDataLength();
	}

	// public void setBuffer(byte[] buffer) {
	// this.buffer = buffer;
	// }

	public void setDecoderArray(BinaryCodec[] decoderArray) {
		this.decoderArray = decoderArray;
	}

}
