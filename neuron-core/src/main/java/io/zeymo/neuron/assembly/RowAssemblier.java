package io.zeymo.neuron.assembly;

import io.zeymo.commons.annotation.ThreadSafe;
import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronOutputBuffer;
import io.zeymo.neuron.NeuronAnnotations;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.io.codec.ObjectCodec;
import io.zeymo.neuron.runtime.NeuronConfiguration;
import io.zeymo.neuron.schema.ColumnLayout;
import io.zeymo.neuron.schema.NodeLayout;
import io.zeymo.neuron.schema.SchemaUtils;
import io.zeymo.neuron.schema.SectorLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

@NeuronAnnotations.NotGCFree
@ThreadSafe
public class RowAssemblier {

	@ThreadSafe
	public static class RowCodec {
		private final ObjectCodec[]					codecIndexMap;
		private final HashMap<String, ObjectCodec>	codecNameMap;
		private final String[]						columnNames;

		public RowCodec(SectorLayout sectorLayout) {
			this.codecNameMap = new HashMap<String, ObjectCodec>();
			this.codecIndexMap = SchemaUtils.createSectorObjectCodecArray(sectorLayout);
			ArrayList<ColumnLayout> referenceList = sectorLayout.getColumnList();
			this.columnNames = new String[referenceList.size()];
			int column = 0;
			for (ColumnLayout reference : referenceList) {
				String name = reference.getName();
				int index = reference.getSequence();
				codecNameMap.put(name, codecIndexMap[index]);
				columnNames[column++] = name;
			}
		}

		public Object[] deserializeToArray(NeuronInputBuffer input) throws IOException {
			Object[] result = new Object[this.codecIndexMap.length];
			int index = 0;
			for (ObjectCodec codec : codecIndexMap) {
				Object data = codec.decode(input);
				result[index] = data;
			}
			return result;
		}

		public HashMap<String, Object> deserializeToMap(NeuronInputBuffer input) throws IOException {
			HashMap<String, Object> result = new HashMap<String, Object>();
			int index = 0;
			for (ObjectCodec codec : codecIndexMap) {
				Object data = codec.decode(input);
				String name = columnNames[index++];
				result.put(name, data);
			}
			return result;
		}

		public void serialize(HashMap<String, Object> data, NeuronOutputBuffer output) throws IOException {
			int index = 0;
			for (String colunmName : columnNames) {
				Object value = data.get(colunmName);
				if (value == null) {
					throw new IllegalArgumentException("could not find value for column: " + colunmName);
				}
				this.codecIndexMap[index++].encode(value, output);
			}
		}

		public void serialize(int index, Object data, NeuronOutputBuffer output) throws IOException {
			ObjectCodec codec = this.codecIndexMap[index];
			codec.encode(data, output);
		}

		public void serialize(Object[] data, NeuronOutputBuffer output) throws IOException {
			for (int index = 0; index < this.codecIndexMap.length; ++index) {
				this.codecIndexMap[index].encode(data[index], output);
			}
		}

		public void serialize(String columnName, Object data, NeuronOutputBuffer output) throws IOException {
			ObjectCodec codec = this.codecNameMap.get(columnName);
			if (codec == null) {
				throw new IllegalArgumentException("could not find codec for column: " + columnName);
			}
			codec.encode(data, output);
		}
	}

	private final HashMap<String, RowCodec>	rowCodecMap;
	private final RowCodec[]				rowCodecs;

	public RowAssemblier(NeuronConfiguration configuration) {
		this.rowCodecs = new RowCodec[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];
		NodeLayout nodeLayout = configuration.getNodeLayout();
		this.rowCodecMap = new HashMap<String, RowCodec>();

		for (SectorLayout sectorLayout : nodeLayout.getSectorLayoutArray()) {
			if (sectorLayout == null) {
				continue;
			}
			int index = sectorLayout.getIndex();
			String name = sectorLayout.getName();
			RowCodec serializer = new RowCodec(sectorLayout);
			rowCodecs[index] = serializer;
			rowCodecMap.put(name, serializer);
		}
	}

	public RowCodec getRowCodec(int sectorIndex) {
		return rowCodecs[sectorIndex];
	}

	public RowCodec getRowCodec(String sectorName) {
		return rowCodecMap.get(sectorName);
	}

}
