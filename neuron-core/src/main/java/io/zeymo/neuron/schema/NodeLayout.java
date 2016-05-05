package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;
import io.zeymo.commons.properties.JsonPropertyUtils;
import io.zeymo.neuron.NeuronConstants;

import java.util.ArrayList;

public class NodeLayout implements JsonPropertyConfigurable {
	private SectorLayout[]	sectorLayoutArray	= new SectorLayout[NeuronConstants.RUNTIME_MAX_SECTOR_COUNT];

	// private SummaryLayout[] summaryLayoutArray = new
	// SummaryLayout[NeuronConstants.RUNTIME_MAX_SUMMARY_COUNT];

	@Override
	public void configure(JsonProperties property) {

		ArrayList<JsonProperties> sectorPropList = property.getArrayNotNull("sector-layout");
		// ArrayList<JsonProperties> summaryPropList =
		// property.getArrayNotNull("summary-layout");

		for (JsonProperties sectorProp : sectorPropList) {
			SectorLayout layout = JsonPropertyUtils.newInstance(SectorLayout.class, sectorProp);

			if (sectorLayoutArray[layout.getIndex()] != null) {
				throw new IllegalArgumentException("duplicated offset for sectorLayout" + property.toPrettyString());
			}

			sectorLayoutArray[layout.getIndex()] = layout;
		}

		// for (JsonProperties summaryProp : summaryPropList) {
		// SummaryLayout layout =
		// JsonPropertyUtils.newInstance(SummaryLayout.class, summaryProp);
		//
		// if (summaryLayoutArray[layout.getIndex()] != null) {
		// throw new
		// IllegalArgumentException("duplicated offset for summaryLayout" +
		// property.toPrettyString());
		// }
		//
		// summaryLayoutArray[layout.getIndex()] = layout;
		// }
	}

	public SectorLayout getSectorLayout(int index) {
		return sectorLayoutArray[index];
	}

	public SectorLayout getSectorLayout(String sectorName) {
		int index = getSectorIndex(sectorName);
		return getSectorLayout(index);
	}

	public int getSectorIndex(String sectorName) {
		for (SectorLayout layout : this.sectorLayoutArray) {
			if (layout != null && sectorName.equals(layout.getName())) {
				return layout.getIndex();
			}
		}
		throw new IllegalArgumentException("unable to find index for sector named : " + sectorName);
	}

	public SectorLayout[] getSectorLayoutArray() {
		return sectorLayoutArray;
	}

	public void setSectorLayoutArray(SectorLayout[] sectorLayoutArray) {
		this.sectorLayoutArray = sectorLayoutArray;
	}
}
