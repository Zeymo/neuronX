package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

public class ColumnLayout implements JsonPropertyConfigurable {

	/**
	 * 用于业务上区分不同的column
	 */
	private String	name;

	// private Class<?> decoderClazz;
	// private Class<?> fieldClazz;
	/**
	 * field在sector中的偏移量
	 */
	private int		sequence;

	private int		serializeId;

	private String	type;

	@Override
	public void configure(JsonProperties property) {
		this.name = property.getStringNotNull("name");
		this.type = property.getStringNotNull("type");
		this.serializeId = property.getIntegerNotNull("sid");
		
		// this.sequence = property.getIntegerNotNull("sequence");
		// try {
		// this.decoderClazz = property.getClazz("decoderClass");
		// this.fieldClazz = property.getClazz("fieldClass");
		// } catch (Exception e) {
		// throw new IllegalArgumentException("unable to initialize by property"
		// + property.toPrettyString(), e);
		// }
	}

	public String getName() {
		return name;
	}

	public int getSequence() {
		return sequence;
	}

	public int getSerializeId() {
		return serializeId;
	}

	public String getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	// public Class<?> getDecoderClazz() {
	// return decoderClazz;
	// }

	public void setSerializeId(int serializeId) {
		this.serializeId = serializeId;
	}

	// public Class<?> getFieldClazz() {
	// return fieldClazz;
	// }
	//
	// public void setFieldClazz(Class<?> fieldClazz) {
	// this.fieldClazz = fieldClazz;
	// }

	public void setType(String name) {
		this.type = name;
	}

	// public void setDecoderClazz(Class<?> decoderClazz) {
	// this.decoderClazz = decoderClazz;
	// }

}
