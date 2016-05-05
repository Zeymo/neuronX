package io.zeymo.neuron.io;

import io.zeymo.neuron.schema.ColumnLayout;
import io.zeymo.neuron.schema.NodeLayout;
import io.zeymo.neuron.schema.SectorLayout;

import java.util.List;

public class SerializeMap {

	/**
	 * 搞成1维数组？ 有空了再说
	 */
	private final int[][]	map;

	public SerializeMap(NodeLayout nodeLayout) {
		SectorLayout[] sa = nodeLayout.getSectorLayoutArray();
		map = new int[sa.length][];
		for (int i = 0; i < sa.length; ++i) {

			SectorLayout layout = sa[i];
			if (layout == null) {
				continue;
			}

			List<ColumnLayout> fieldList = layout.getColumnList();
			int fieldCount = fieldList.size();

			int maxIndex = 0;
			for (int j = 0; j < fieldCount; ++j) {
				ColumnLayout colLayout = fieldList.get(j);
				int sid = colLayout.getSerializeId();
				maxIndex = maxIndex < sid ? sid : maxIndex;
			}

			map[i] = new int[maxIndex + 1];

			for (int j = 0; j < fieldCount; ++j) {
				ColumnLayout colLayout = fieldList.get(j);
				int fieldIndex = colLayout.getSequence();
				int sid = colLayout.getSerializeId();

				map[i][sid] = fieldIndex;
			}
		}
	}

	public int getFieldIndex(int sectorIndex, int fieldIndex) {
		return map[sectorIndex][fieldIndex];
	}
}
