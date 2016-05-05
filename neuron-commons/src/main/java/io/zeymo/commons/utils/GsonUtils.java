package io.zeymo.commons.utils;

import com.google.gson.*;

public class GsonUtils {
	public static String toString(Object object) {
		Gson gson = new Gson();
		return gson.toJson(object);
	}

	public static JsonElement toJsonObject(Object object) {
		Gson gson = new Gson();
		return gson.toJsonTree(object);
	}

	public static JsonElement parseJsonObject(String jsonString) {
		JsonParser parser = new JsonParser();
		return parser.parse(jsonString);
	}

	public static String toPrettyString(Object object) {
		if (object == null) {
			return null;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(object);
	}

	public static String toString(JsonObject object) {
		if (object == null) {
			return null;
		}
		Gson gson = new Gson();
		return gson.toJson(object);
	}

	public static String toPrettytring(JsonObject object) {
		if (object == null) {
			return null;
		}
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(object);
	}

	public static <T> T fromString(String gsonString, Class<T> clazz) {
		if (gsonString == null) {
			return null;
		}
		Gson gson = new Gson();
		return gson.fromJson(gsonString, clazz);
	}
}