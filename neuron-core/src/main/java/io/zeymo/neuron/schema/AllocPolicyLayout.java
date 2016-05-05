package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

public class AllocPolicyLayout implements JsonPropertyConfigurable {

	private Class<?>		allocPolicyClazz;
	private JsonProperties properties;

	@Override
	public void configure(JsonProperties property) {
		try {
			this.allocPolicyClazz = property.getClazzNotNull("class");
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("unable to initialize alloc policy : " + property.getString("class"), e);
		}
		this.properties = property.getSubProperties("properties");
	}

	public Class<?> getAllocPolicyClazz() {
		return allocPolicyClazz;
	}

	public JsonProperties getProperties() {
		return properties;
	}

	public void setAllocPolicyClazz(Class<?> allocPolicyClazz) {
		this.allocPolicyClazz = allocPolicyClazz;
	}

	public void setProperties(JsonProperties properties) {
		this.properties = properties;
	}

}
