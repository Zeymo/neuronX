package io.zeymo.neuron.assembly.domain;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.domain.Node;
import io.zeymo.neuron.runtime.NeuronCodecRuntime;
import io.zeymo.neuron.runtime.NeuronConfiguration;
import io.zeymo.neuron.schema.NodeLayout;
import io.zeymo.neuron.schema.SectorLayout;

import java.io.IOException;

public class XNode {
	// private final NeuronCodecRuntime codecRuntime;
	private long			nodeId;
	// private final XRowAssemblier[] rowAssembliers = new XRowAssemblier[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
	// private SearchParam searchParam;
	private final int		sectorCount;
	private final int[]		sectorIndexies	= new int[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
	private final XSector[]	sectors			= new XSector[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
	private int				status;

	private int				version;

	public XNode(NeuronConfiguration configuration) {
		// this.codecRuntime = codecRuntime;

		NodeLayout nodeLayout = configuration.getNodeLayout();
		SectorLayout[] sectorLayouts = nodeLayout.getSectorLayoutArray();
		int sectorCount = 0;
		for (int sectorIndex = 0; sectorIndex < sectorLayouts.length; ++sectorIndex) {
			if (sectorLayouts[sectorIndex] == null) {
				continue;
			}
			sectors[sectorIndex] = new XSector(configuration, sectorIndex);
			// rowAssembliers[sectorIndex] = new XRowAssemblier(configuration, sectorIndex);
			this.sectorIndexies[sectorCount++] = sectorIndex;
		}
		// this.searchParam = new SearchParam();
		this.sectorCount = sectorCount;
	}

	public void appendRows(NeuronInputBuffer inputBuffer) throws IOException {
		// int length = inputBuffer.readUVInt();
		// int limit = inputBuffer.limit(length);

		while (inputBuffer.hasNext()) {
			int sectorIndex = inputBuffer.readUVInt();
			XSector sector = this.getSector(sectorIndex);
			sector.appendRow(inputBuffer);
		}
	}

	/**
	 * 构造出一个Node对象的序列化数据，注意会自动调用version+1
	 * 
	 * @param writer
	 * @return
	 * @throws IOException
	 */
	public int build(NeuronWriter writer) throws IOException {
		writer.writeFLong(nodeId);
		writer.writeFInt(status);
		writer.writeFInt(version + 1);
		writer.writeByte(sectorCount);

		int sectorSize = 0;

		for (int i = 0; i < this.sectorCount; ++i) {
			int sectorIndex = this.sectorIndexies[i];
			sectorSize += this.sectors[sectorIndex].build(writer);
		}
		return 8 + 8 + 1 + sectorSize;
	}

	// public void deleteRows(MpInputBuffer inputBuffer) throws IOException {
	// // int limit = inputBuffer.limit(inputLength);
	//
	// while (inputBuffer.hasNext()) {
	// // int sectorIndex = searchParam.getSectorIndex();// inputBuffer.readUVInt();
	// searchParam.readFields(inputBuffer);
	// int sectorIndex = searchParam.getSectorIndex();
	//
	// XSector sector = this.getSector(sectorIndex);
	// sector.deleteRows(searchParam);
	// }
	// }

	public void deleteSectors(NeuronInputBuffer inputBuffer) throws IOException {
		// int limit = inputBuffer.limit(inputLength);

		while (inputBuffer.hasNext()) {
			int sectorIndex = inputBuffer.readUVInt();
			XSector sector = this.getSector(sectorIndex);
			sector.clear();
		}
	}

	public long getNodeId() {
		return nodeId;
	}

	// public XRowAssemblier getRowAssemblier(int sectorIndex) {
	// return this.rowAssembliers[sectorIndex];
	// }

	public XSector getSector(int sectorIndex) {
		return this.sectors[sectorIndex];
	}

	public void putSectors(NeuronInputBuffer inputBuffer) throws IOException {
		while (inputBuffer.hasNext()) {
			int sectorIndex = inputBuffer.readUVInt();
			int rowCount = inputBuffer.readUVInt();
			XSector sector = this.getSector(sectorIndex);

			sector.clear();
			while (rowCount-- > 0) {
				sector.appendRow(inputBuffer);
			}
		}

	}

	// public void putRows(MpInputBuffer inputBuffer, boolean append) throws IOException {
	// // int limit = inputBuffer.limit(inputLength);
	//
	// while (inputBuffer.hasNext()) {
	// searchParam.readFields(inputBuffer);
	// int sectorIndex = searchParam.getSectorIndex();
	//
	// XSector sector = this.getSector(sectorIndex);
	// sector.putRow(searchParam, append, inputBuffer);
	// }
	// }

	public void reset(long node) {

		this.nodeId = node;
		this.version = 0;
		this.status = 0;
		for (int i = 0; i < this.sectorCount; ++i) {
			final int sectorIndex = sectorIndexies[i];
			this.sectors[sectorIndex].clear();
		}
	}

	/**
	 * 
	 * @throws IOException
	 */
	public void reset(NeuronCodecRuntime codecRuntime) throws IOException {

		Node node = codecRuntime.getNode();

		this.nodeId = node.getNodeId();
		this.status = node.getStatus();
		this.version = node.getVersion();

		for (int i = 0; i < this.sectorCount; ++i) {
			int sectorIndex = this.sectorIndexies[i];
			this.sectors[sectorIndex].load(codecRuntime);
		}
	}

}
