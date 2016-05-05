package io.zeymo.commons.properties;

import com.google.gson.JsonObject;
import io.zeymo.commons.codec.CodecUtils;
import io.zeymo.commons.utils.GsonUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JsonPropertyUtils {
	private static final Map<Class<?>, Constructor<?>>	CONSTRUCTOR_CACHE	= new ConcurrentHashMap<Class<?>, Constructor<?>>();

	private static final Class<?>[]						emptyArray			= new Class[] {};

	private static HashMap<String, Class<?>>			supportedTypeMap	= new HashMap<String, Class<?>>();

	static {
		supportedTypeMap.put("boolean", boolean.class);
		supportedTypeMap.put("integer", int.class);
		supportedTypeMap.put("int", int.class);
		supportedTypeMap.put("long", long.class);
		supportedTypeMap.put("double", double.class);
		supportedTypeMap.put("string", String.class);
		supportedTypeMap.put("float", float.class);
		supportedTypeMap.put("short", short.class);
		supportedTypeMap.put("byte", byte.class);
	}

	public static Class<?> getClass(String name) {
		try {
			return ClassUtils.getClass(name);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public static <U> Class<? extends U> getClass(String name, Class<U> xface) {
		try {
			Class<?> theClass = getClass(name);
			if (theClass != null && !xface.isAssignableFrom(theClass))
				throw new RuntimeException(theClass + " not " + xface.getName());
			else if (theClass != null)
				return theClass.asSubclass(xface);
			else
				return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static JsonProperties loadProperty(File input) throws IOException {
		JsonObject jsonObject = loadJsonObject(input);
		return new JsonProperties(jsonObject);
	}

	public static JsonProperties loadProperty(InputStream input) throws IOException {
		JsonObject jsonObject = loadJsonObject(input);
		return new JsonProperties(jsonObject);
	}

	public static JsonProperties loadProperty(Reader reader) throws IOException {
		JsonObject jsonObject = loadJsonObject(reader);
		return new JsonProperties(jsonObject);
	}

	public static JsonProperties loadProperty(String jsonString) throws IOException {
		JsonObject jsonObject = loadJsonObject(jsonString);
		return new JsonProperties(jsonObject);
	}

	public static JsonObject loadJsonObject(File file) throws IOException {
		return loadJsonObject(new FileInputStream(file));
	}

	public static JsonObject loadJsonObject(InputStream inputStream) throws IOException {
		Reader reader = new InputStreamReader(inputStream);
		return loadJsonObject(reader);
	}

	public static JsonObject loadJsonObject(Reader reader) throws IOException {
		StringWriter writer = new StringWriter();
		IOUtils.copy(reader, writer);
		String theString = writer.toString();
		reader.close();

		return GsonUtils.parseJsonObject(theString).getAsJsonObject();
	}

	public static JsonObject loadJsonObject(String jsonString) {
		try {
			return loadJsonObject(new StringReader(jsonString));
		} catch (IOException e) {
			throw new RuntimeException("StringReader failed, should never happen.");
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> theClass) {
		T result;
		try {
			Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
			if (meth == null) {
				meth = theClass.getDeclaredConstructor(emptyArray);
				// meth.setAccessible(true);
				CONSTRUCTOR_CACHE.put(theClass, meth);
			}
			result = meth.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(Class<T> theClass, JsonProperties conf) {
		T result;
		try {
			Constructor<T> meth = (Constructor<T>) CONSTRUCTOR_CACHE.get(theClass);
			if (meth == null) {
				if (JsonPropertyConstructable.class.isAssignableFrom(theClass)) {
					meth = theClass.getDeclaredConstructor(JsonProperties.class);
				} else {
					meth = theClass.getDeclaredConstructor(emptyArray);
				}
				CONSTRUCTOR_CACHE.put(theClass, meth);
			}
			if (JsonPropertyConstructable.class.isAssignableFrom(theClass)) {
				result = meth.newInstance(conf);
			} else {
				result = meth.newInstance();
				setProperty(result, conf);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String theClass) {
		Class<T> clazz = null;
		try {
			clazz = ClassUtils.getClass(theClass);
		} catch (ClassNotFoundException e) {
			// logger.error("无法初始化类实例" + theClass, e);
		}
		T result = newInstance(clazz);
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T newInstance(String theClass, JsonProperties property) {
		Class<T> clazz = null;
		try {
			clazz = ClassUtils.getClass(theClass);
		} catch (ClassNotFoundException e) {
			// logger.error("无法加载类 " + theClass + ", 当前ClassLoader" +
			// Thread.currentThread().getContextClassLoader(), e);
			throw new RuntimeException(e);
		}
		return newInstance(clazz, property);

	}

	public static OutputStream openOutputStream(File file) throws IOException {
		return new FileOutputStream(file);
	}

	public static Object parseText(String typeName, String input) {
		input = input.trim().toLowerCase();

		Class<?> type = supportedTypeMap.get(typeName);
		if (typeName == null) {
			throw new IllegalArgumentException("无法识别的参数类型" + typeName);
		}

		if (type == String.class) {
			return input;
		}

		if (input.length() == 0) {
			return null;
		}

		if (type == boolean.class) {
			if (StringUtils.isNumeric(input)) {
				return Double.parseDouble(input) > 0 ? true : false;
			}
			return Boolean.parseBoolean(input);
		}

		if (type == double.class) {
			return Double.parseDouble(input);
		}

		if (type == int.class) {
			int pos = input.indexOf('.');
			if (pos >= 0) {
				input = input.substring(0, pos);
			}
			return Integer.parseInt(input);
		}

		if (type == long.class) {
			int pos = input.indexOf('.');
			if (pos >= 0) {
				input = input.substring(0, pos);
			}
			return Long.parseLong(input);
		}

		if (type == byte.class) {
			int pos = input.indexOf('.');
			if (pos >= 0) {
				input = input.substring(0, pos);
			}
			return Byte.parseByte(input);
		}

		if (type == short.class) {
			int pos = input.indexOf('.');
			if (pos >= 0) {
				input = input.substring(0, pos);
			}
			return Short.parseShort(input);
		}

		if (type == float.class) {
			return Float.parseFloat(input);
		}

		throw new IllegalArgumentException("无法识别的参数类型" + typeName);

	}

	public static void setProperty(Object theObject, JsonProperties property) {
		if (property != null) {
			if (theObject instanceof JsonPropertyConfigurable) {
				((JsonPropertyConfigurable) theObject).configure(property);
			}
		}
	}

	public static String toCompactJsonString(JsonProperties p) {
		return p.toString();
	}

	/**
	 * 转为用JSON表示的featureString，先转JSON、压缩，再base64
	 * 
	 * @param p
	 * @return
	 */
	public static String toCompressedJson64String(JsonProperties p) {

		String jsonString = p.toString();
		try {
			return CodecUtils.packToBase64String(jsonString);
		} catch (IOException e) {
			throw new IllegalArgumentException("无法处理的property" + p.toString());
		}
	}

	/**
	 * 用JSON表示的featureString，先解base64，再解压缩，再转回对象
	 * 
	 * @param feature
	 * @return
	 */
	public static JsonProperties fromCompressedJson64String(String feature) {
		try {
			String jsonString = CodecUtils.getDecompressedStringFromBase64(feature);
			return JsonProperties.fromString(jsonString);
		} catch (IOException e) {
			throw new IllegalArgumentException("无法解析的压缩参数" + feature);
		}
	}

	public static String toPrettyJsonString(JsonProperties p) {
		return p.toPrettyString();
	}

}
