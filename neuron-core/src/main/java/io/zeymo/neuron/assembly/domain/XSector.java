package io.zeymo.neuron.assembly.domain;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.domain.SectorIterator;
import io.zeymo.neuron.runtime.NeuronCodecRuntime;
import io.zeymo.neuron.runtime.NeuronConfiguration;

import java.io.IOException;

public class XSector {
	private byte[]		buffer;
	// private NeuronCodecRuntime codecRuntime;
	private int			index;
	private XRow[]		rowArray	= new XRow[NeuronConstants.RUNTIME_MAX_SECTOR_ROW_COUNT];
	private int			rowCount;
	private final int	rowMod		= NeuronConstants.RUNTIME_MAX_SECTOR_ROW_COUNT;

	// private int rowStart;

	// private NeuronMatcher searcher;

	public XSector(NeuronConfiguration configuration, int sectorIndex) {
		this.index = sectorIndex;
		// this.codecRuntime = codecRuntime;
		// this.searcher = new NeuronMatcher(configuration);
		for (int i = 0; i < rowMod; ++i) {
			this.rowArray[i] = new XRow();
		}
	}

	public int getRowCount() {
		return rowCount;
	}

	public XRow getRow(int rowNumber) {
		int index = rowNumber % rowMod;
		return rowArray[index];
	}

	public void deleteRow(int rowNumber) {
		int index = rowNumber % rowMod;
		rowArray[index].setDelete(true);
	}

	public void appendRow(byte[] buffer, int offset, int length) {
		// int rowIndex = rowCount % rowMod;
		// if (this.rowCount > rowMod) {
		// this.rowStart = (this.rowStart + 1) % rowMod;
		// this.rowCount = rowMod;
		// }
		XRow row = this.rowArray[rowCount % rowMod];
		row.set(buffer, offset, length);
		++rowCount;
	}

	// public void appendRows(MpInputBuffer inputBuffer) throws IOException {
	//
	// while (inputBuffer.hasNext()) {
	// int rowIndex = rowStart + rowCount;
	// ++rowCount;
	//
	// if (this.rowCount > rowMod) {
	// this.rowStart = (this.rowStart + 1) % rowMod;
	// this.rowCount = rowMod;
	// }
	//
	// XRow row = this.rowArray[rowIndex % rowMod];
	// row.set(inputBuffer);
	// }
	// }

	public void appendRow(NeuronInputBuffer inputBuffer) throws IOException {
		// int rowIndex = rowStart + rowCount;
		// ++rowCount;
		// if (this.rowCount > rowMod) {
		// this.rowStart = (this.rowStart + 1) % rowMod;
		// this.rowCount = rowMod;
		// }

		XRow row = this.rowArray[rowCount % rowMod];
		row.set(inputBuffer);
		++rowCount;

	}

	public int build(NeuronWriter outputWriter) throws IOException {

		int dataLength = 0;

		int count = 0;

		final int rowStart;
		final int limit;

		if (rowCount >= rowMod) {
			rowStart = rowCount % rowMod;
			limit = rowStart + rowMod;
		} else {
			rowStart = 0;
			limit = rowCount;
		}

		for (int i = rowStart; i < limit; ++i) {
			XRow row = this.rowArray[i % rowMod];
			if (!row.isDelete()) {
				dataLength += row.getLength();
				count++;
			}
		}

		int offset = outputWriter.getRelativeOffset();

		outputWriter.writeByte(this.index);
		outputWriter.writeUVInt(count);
		outputWriter.writeUVInt(dataLength);

		for (int i = rowStart; i < limit; ++i) {
			XRow row = this.rowArray[i % rowMod];
			if (!row.isDelete())
				outputWriter.writeBytes(row.getBuffer(), 0, row.getLength());
		}
		return outputWriter.getRelativeOffset() - offset;
	}

	public void clear() {
		// this.rowStart = 0;
		this.rowCount = 0;
	}

	// public void deleteRows(SearchParam searchParam) throws IOException {
	// searcher.reset(searchParam);
	// final UnionBuffer unionBuffer = this.codecRuntime.getUnionBuffer();
	// final SectorIterator iterator = this.codecRuntime.getIterator(index);
	//
	// int rowCount = 0;
	// for (int i = rowStart; i < rowStart + this.rowCount; ++i) {
	// int rowNumber = i % rowMod;
	// XRow row = this.rowArray[rowNumber];
	//
	// if (!row.isDelete()) {
	// rowCount++;
	// iterator.loadExternal(row.getBuffer(), 0, row.getLength());
	// if (searcher.matchCurrent(unionBuffer, row.getBuffer())) {
	// this.rowArray[i++].setDelete(true);
	// rowCount--;
	// }
	// }
	// }
	// this.rowCount = rowCount;
	// }

	// public void deleteRows(MpInputBuffer inputBuffer) throws IOException {
	// int length = inputBuffer.readUVInt();
	// int limit = inputBuffer.limit(length);
	//
	// while (inputBuffer.hasNext(limit)) {
	// int row = inputBuffer.readUVInt();
	//
	// // 已经被挤出去的不算
	// if (row < this.rowStart) {
	// continue;
	// }
	//
	// this.rowArray[row % rowMod].setDelete(true);
	// }
	// }

	// public String getName() {
	// return name;
	// }

	public void load(NeuronCodecRuntime codecRuntime) throws IOException {
		this.buffer = codecRuntime.getNode().getBuffer();

		final SectorIterator iterator = codecRuntime.getIterator(index);

		int count = 0;
		while (iterator.next()) {

			int offset = iterator.getCurrentOffset();
			int length = iterator.getCurrentLength();

			this.rowArray[count++].set(buffer, offset, length);
		}
		this.rowCount = count;
	}

	// public void putRow(SearchParam searchParam, boolean append, MpInputBuffer inputBuffer) throws IOException {
	//
	// searcher.reset(searchParam);
	// final UnionBuffer unionBuffer = codecRuntime.getUnionBuffer();
	// final SectorIterator iterator = codecRuntime.getIterator(index);
	//
	// boolean found = false;
	//
	// for (int i = rowStart; i < rowStart + this.rowCount; ++i) {
	// int rowNumber = i % rowMod;
	// XRow row = this.rowArray[rowNumber];
	//
	// // replace only one on row exists.
	// if (!row.isDelete()) {
	// iterator.loadExternal(row.getBuffer(), 0, row.getLength());
	// if (searcher.matchCurrent(unionBuffer, row.getBuffer())) {
	// this.rowArray[i++].set(inputBuffer);
	// found = true;
	// break;
	// }
	// }
	// }
	//
	// // append when not exist
	// if (!found && append) {
	// this.appendRow(inputBuffer);
	// }
	// }

}
