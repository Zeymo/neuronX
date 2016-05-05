package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;
import io.zeymo.commons.properties.JsonPropertyUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SectorLayout implements JsonPropertyConfigurable {

	private int								index;
	private String							name;
	private ArrayList<ColumnLayout>			columnList;

	private HashMap<String, ColumnLayout>	columnMap;

	@Override
	public void configure(JsonProperties property) {
		this.columnList = new ArrayList<ColumnLayout>();
		this.columnMap = new HashMap<String, ColumnLayout>();
		this.index = property.getIntegerNotNull("index");
		this.name = property.getStringNotNull("name");

		ArrayList<JsonProperties> fieldPropList = property.getArrayNotNull("columns");

		int i = 0;
		for (JsonProperties fieldProp : fieldPropList) {
			ColumnLayout column = JsonPropertyUtils.newInstance(ColumnLayout.class, fieldProp);
			column.setSequence(i);
			// if (field.getSequence() != i) {
			// throw new IllegalArgumentException("field " + field.getName() + " offset/sequence not match " + field.getSequence() + ", should be " + i + "," + property.toPrettyString());
			// }
			columnList.add(column);
			columnMap.put(column.getName(), column);
			++i;
		}

	}

	public ColumnLayout getColumnLayoutByIndex(int index) {
		return columnList.get(index);
	}

	public ColumnLayout getColumnLayoutBySID(int sid) {
		for (ColumnLayout c : columnList) {
			if (c.getSerializeId() == sid) {
				return c;
			}
		}
		return null;
	}

	public ColumnLayout getColumnLayoutByName(String columnName) {
		return columnMap.get(columnName);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<ColumnLayout> getColumnList() {
		return columnList;
	}

	public void setColumnList(ArrayList<ColumnLayout> columnList) {
		this.columnList = columnList;
	}

}
