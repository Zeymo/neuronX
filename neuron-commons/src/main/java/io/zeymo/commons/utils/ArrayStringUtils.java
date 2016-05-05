package io.zeymo.commons.utils;

public class ArrayStringUtils {
	public static String toString(long[] array, int start, int length) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (length > 0) {
			sb.append(array[start]);
		}
		for (int i = start + 1; i < start + length; ++i) {
			sb.append(",");
			sb.append(array[i]);
		}
		sb.append("]");
		return sb.toString();
	}
}
