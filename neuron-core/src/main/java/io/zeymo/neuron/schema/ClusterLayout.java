package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

public class ClusterLayout implements JsonPropertyConfigurable {
	private JsonProperties	properties;

	public JsonProperties getProperties() {
		return properties;
	}

	public void setProperties(JsonProperties properties) {
		this.properties = properties;
	}

	@Override
	public void configure(JsonProperties property) {
		this.properties = property;
	}

}
