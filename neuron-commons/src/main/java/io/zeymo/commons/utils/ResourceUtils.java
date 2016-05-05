package io.zeymo.commons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ResourceUtils {

	public final static Charset	charset	= Charset.forName("UTF-8");

	public static String getResourceAsString(String path) {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		return getResourceAsString(path, cl, charset);
	}

	public static String getResourceAsString(String path, Class<?> clazz) {
		return getResourceAsString(path, clazz, charset);
	}

	public static String getResourceAsString(String path, Class<?> clazz, Charset charset) {
		ClassLoader cl = clazz.getClassLoader();
		return getResourceAsString(path, cl, charset);
	}

	public static String getResourceAsString(String path, ClassLoader cl) {
		return getResourceAsString(path, cl, charset);
	}

	public static String getResourceAsString(String path, ClassLoader cl, Charset charset) {
		InputStream is = cl.getResourceAsStream(path);
		InputStreamReader reader = new InputStreamReader(is, charset);

		StringBuilder sb = new StringBuilder();

		char[] c = new char[1024];
		try {
			for (int n; (n = reader.read(c)) != -1;) {
				sb.append(new String(c, 0, n));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return sb.toString();
	}
}
