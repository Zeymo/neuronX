/**
 * 
 */
package io.zeymo.commons.utils;

import java.io.*;
import java.nio.charset.Charset;

public class FileUtils {

	public final static Charset	charset	= Charset.forName("UTF-8");

	public static String getAsString(File file) {
		return getAsString(file, charset);
	}

	public static String getAsString(File file, Charset charset) {
		try {
			FileInputStream fis = new FileInputStream(file);
			return getAsString(fis, charset);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getAsString(InputStream is) {
		return getAsString(is, charset);
	}

	public static String getAsString(InputStream is, Charset charset) {
		BufferedInputStream bis = new BufferedInputStream(is, 4096);
		InputStreamReader reader = new InputStreamReader(bis, charset);

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

	public static String getAsString(String file) {
		return getAsString(new File(file), charset);
	}

	public static String getAsString(String file, Charset charset) {
		return getAsString(new File(file), charset);
	}
}
