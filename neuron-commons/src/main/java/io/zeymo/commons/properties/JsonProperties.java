package io.zeymo.commons.properties;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.zeymo.commons.utils.GsonUtils;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

public class JsonProperties {

	public static JsonProperties fromString(String jsonString) {
		JsonElement element = GsonUtils.parseJsonObject(jsonString);
		return new JsonProperties(element.getAsJsonObject());
	}

	private JsonObject	json;

	public JsonProperties() {

		this.json = new JsonObject();
	}

	public JsonProperties(JsonObject json) {
		this.json = json;
	}

	public ArrayList<JsonProperties> getArray(String name) {
		ArrayList<JsonProperties> subArray = new ArrayList<JsonProperties>();
		JsonArray array = json.get(name).getAsJsonArray();

		for (JsonElement element : array) {
			subArray.add(new JsonProperties(element.getAsJsonObject()));
		}

		return subArray;
	}

	public ArrayList<JsonProperties> getArrayNotNull(String name) {
		if (json.get(name) == null || !json.get(name).isJsonArray()) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return getArray(name);
	}

	public Boolean getBoolean(String name) {
		return getBoolean(name, null);
	}

	public Boolean getBoolean(String name, Boolean defaultValue) {

		String value = getString(name);
		if (value == null) {
			return defaultValue;
		}
		return Boolean.parseBoolean(value);
	}

	public Boolean getBooleanNotNull(String name) {
		Boolean value = getBoolean(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public Double getDouble(String name) {
		return getDouble(name, null);
	}

	public Double getDouble(String name, Double defaultValue) {
		String value = getString(name);
		if (value == null) {
			return defaultValue;
		}
		return Double.parseDouble(value);
	}

	public Double getDoubleNotNull(String name) {
		Double value = getDouble(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public Float getFloat(String name) {
		return getFloat(name, null);
	}

	public Float getFloat(String name, Float defaultValue) {

		String value = getString(name);
		if (value == null) {
			return defaultValue;
		}
		return Float.parseFloat(value);
	}

	public Float getFloatNotNull(String name) {
		Float value = getFloat(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public Integer getInteger(String name) {
		return getInteger(name, null);
	}

	public Integer getInteger(String name, Integer defaultValue) {
		String value = getString(name);
		if (value == null) {
			return defaultValue;
		}
		return Integer.parseInt(value);
	}

	public Integer getIntegerNotNull(String name) {
		Integer value = getInteger(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public Long getLong(String name) {
		return getLong(name, null);
	}

	public Long getLong(String name, Long defaultValue) {
		String value = getString(name);
		if (value == null) {
			return defaultValue;
		}
		return Long.parseLong(value);
	}

	public Long getLongNotNull(String name) {
		Long value = getLong(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public ArrayList<String> getMemberNames() {
		Set<Entry<String, JsonElement>> subEntrySet = json.entrySet();
		ArrayList<String> result = new ArrayList<String>(subEntrySet.size());

		for (Entry<String, JsonElement> subEntry : subEntrySet) {
			result.add(subEntry.getKey());
		}
		return result;

	}

	public String getString(String name) {
		return getString(name, null);
	}

	public String getString(String name, String defaultValue) {
		if (this.json == null) {
			return defaultValue;
		}
		JsonElement element = this.json.get(name);
		if (element == null) {
			return defaultValue;
		}
		return element.getAsString();
	}

	public String getStringNotNull(String name) {
		String value = getString(name, null);
		if (value == null) {
			throw new IllegalArgumentException("value must be NOT NULL for key : " + name);
		}
		return value;
	}

	public <T> Class<T> getClazz(String name) throws ClassNotFoundException {
		String value = getString(name);
		if (value == null) {
			return null;
		}
		return getClazzNotNull(name);
	}

	@SuppressWarnings("unchecked")
	public <T> Class<T> getClazzNotNull(String name) throws ClassNotFoundException {
		return (Class<T>) Class.forName(getStringNotNull(name));
	}

	public JsonProperties getSubProperties(String name) {
		JsonObject jsonObject = json.getAsJsonObject(name);
		return new JsonProperties(jsonObject);
	}

	public void remove(String name) {
		this.json.remove(name);
	}

	public void setArray(String name, ArrayList<JsonProperties> array) {
		if (array == null) {
			remove(name);
			return;
		}

		JsonArray jsonArray = new JsonArray();
		for (JsonProperties element : array) {
			jsonArray.add(element.json);
		}

		this.remove(name);
		json.add(name, jsonArray);
	}

	public void setBoolean(String name, Boolean value) {
		if (value == null) {
			remove(name);
			return;
		}
		String string = Boolean.toString(value);
		this.setString(name, string);
	}

	public void setDouble(String name, Double value) {
		if (value == null) {
			remove(name);
			return;
		}
		String string = Double.toString(value);
		this.setString(name, string);
	}

	public void setFloat(String name, Float value) {
		if (value == null) {
			remove(name);
			return;
		}
		String string = Float.toString(value);
		this.setString(name, string);
	}

	public void setInteger(String name, Integer value) {
		if (value == null) {
			remove(name);
			return;
		}
		String string = Integer.toString(value);
		this.setString(name, string);
	}

	public void setLong(String name, Long value) {
		if (value == null) {
			remove(name);
			return;
		}
		String string = Long.toString(value);
		this.setString(name, string);
	}

	public void setString(String name, String value) {
		if (value == null) {
			remove(name);
			return;
		}
		json.remove(name);
		json.addProperty(name, value);
	}

	public void setSubProperties(String name, JsonProperties subProperties) {
		json.remove(name);
		json.add(name, subProperties.json);
	}

	public String toPrettyString() {
		return GsonUtils.toPrettyString(this.json);
	}

	@Override
	public String toString() {
		return GsonUtils.toString(this.json);
	}

}
