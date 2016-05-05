package io.zeymo.neuron.domain;

import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.neuron.NeuronAnnotations;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.io.BufferParam;

import java.io.IOException;


@NeuronAnnotations.GCFree
public class Node {

	private byte[]		buffer;

	private int			length;

	/**
	 * 在数据中存储使用定长int64表示，不然缓存漂移之类的问题要查死
	 */
	private long		nodeId;
	private int			offset;
	private Sector[]	sectorArray;
	private int			sectorCount;
	private int			status;
	private int			version;

	public Node() {
		this.sectorArray = new Sector[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
		for (int i = 0; i < NeuronConstants.RUNTIME_MAX_SECTOR_COUNT; ++i) {
			this.sectorArray[i] = new Sector();
		}

		// this.sectorLengthArray = new
		// int[NeuronConstants.DEFAULT_MAX_SECTOR_COUNT];
		// this.sectorOffsetArray = new
		// int[NeuronConstants.DEFAULT_MAX_SECTOR_COUNT];
	}

	public byte[] getBuffer() {
		return buffer;
	}

	// private int[] sectorLengthArray;
	// private int[] sectorOffsetArray;

	public int getLength() {
		return length;
	}

	public long getNodeId() {
		return nodeId;
	}

	public int getOffset() {
		return offset;
	}

	public Sector getSector(int sectorIndex) {
		return sectorArray[sectorIndex];
	}

	public Sector[] getSectorArray() {
		return sectorArray;
	}

	public int getSectorCount() {
		return sectorCount;
	}

	public int getStatus() {
		return status;
	}

	public int getVersion() {
		return version;
	}

	// public int[] getSectorLengthArray() {
	// return sectorLengthArray;
	// }
	//
	// public int[] getSectorOffsetArray() {
	// return sectorOffsetArray;
	// }

	// @Override

	public final static int	NODE_STATUS_NORMAL	= 0;
	public final static int	NODE_STATUS_DELETED	= 4;
	public final static int	VERSION_OFFSET		= 12;
	public final static int	STATUS_OFFSET		= 8;
	public final static int	ID_OFFSET			= 0;

	public final static long parseId(BufferParam param) {
		return NeuronReader.readRawInt64(param.getBuffer(), ID_OFFSET);
	}

	public final static void setId(BufferParam param, long id) {
		NeuronWriter.writeFLong(param.getBuffer(), ID_OFFSET, id);
	}

	public final static int parseVersion(BufferParam param) {
		return NeuronReader.readRawInt32(param.getBuffer(), VERSION_OFFSET);
	}

	public final static int parseStatus(BufferParam param) {
		return NeuronReader.readRawInt32(param.getBuffer(), STATUS_OFFSET);
	}

	public final static void setVersion(BufferParam param, int version) {
		NeuronWriter.writeFInt(param.getBuffer(), VERSION_OFFSET, version);
	}

	public final static void setStatus(BufferParam param, int status) {
		NeuronWriter.writeFInt(param.getBuffer(), STATUS_OFFSET, status);
	}

	public int reset(NeuronByteArrayReader reader, byte[] buffer, int offset, int length) throws IOException {
		NeuronReader.reuse(reader, buffer, offset, length);

		this.buffer = buffer;
		this.offset = offset;
		this.length = length;

		this.nodeId = reader.readFLong();

		this.status = reader.readFInt();
		this.version = reader.readFInt();
		this.sectorCount = reader.readByte();

		// int off = 0;
		// for (int i = 0; i < this.sectorCount; ++i) {
		// int len = reader.readUVInt();
		// this.sectorLengthArray[i] = len;
		// this.sectorOffsetArray[i] = off;
		// off += len;
		// }

		// beginning of sections
		// int position = reader.getPosition();

		for (int i = 0; i < this.sectorCount; ++i) {
			// int sectorOffset = position + this.sectorOffsetArray[i];
			// reader.setOffset(sectorOffset);

			int sectorBegin = reader.getPosition();
			int sectorTypeIndex = reader.readByte();

			// int sectorLength = this.sectorLengthArray[i];
			// reader.setPosition(sectorOffset);

			Sector sector = this.sectorArray[sectorTypeIndex];
			int sectorSize = sector.reset(reader, buffer, sectorBegin, buffer.length - sectorBegin);

			// sectorOffsetArray[sectorTypeIndex] = sectorBegin;
			// sectorLengthArray[sectorTypeIndex] = sector.getDataLength();

			reader.setPosition(sectorBegin + sectorSize);
		}

		return reader.getPosition() - offset;
	}

	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public void setSectorArray(Sector[] sectorArray) {
		this.sectorArray = sectorArray;
	}

	public void setSectorCount(int sectorCount) {
		this.sectorCount = sectorCount;
	}

	// public void setSectorLengthArray(int[] sectorLengthArray) {
	// this.sectorLengthArray = sectorLengthArray;
	// }
	//
	// public void setSectorOffsetArray(int[] sectorOffsetArray) {
	// this.sectorOffsetArray = sectorOffsetArray;
	// }
}
