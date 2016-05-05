package io.zeymo.commons.utils;


import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.impl.NeuronByteArrayWriter;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.TreeMap;

public class ByteUtils {

	public static class Metrics {
		TreeMap<Integer, String>	matricsMap	= new TreeMap<Integer, String>();

		public void append(int offset, String tag) {
			matricsMap.put(offset, tag);
		}

		public void append(NeuronByteArrayWriter writer, String tag) {
			matricsMap.put(writer.getPosition(), tag);
		}

	}

	public static boolean compare(byte[] left, byte[] right) {
		int length = left.length;
		if (right.length != left.length) {
			return false;
		}
		return compare(left, 0, right, 0, length);
	}

	public static boolean compare(byte[] left, int leftOff, byte[] right, int rightOff, int length) {
		while (length-- > 0) {
			if (left[leftOff++] != right[rightOff++]) {
				return false;
			}
		}
		return true;

	}

	/**
	 * ByteBuffer@Heap Direct copy，不检测limit，不改动position，慎用
	 * @param src
	 * @param srcPos
	 * @param dest
	 * @param destPos
	 * @param length
	 */
	public static void copyBytes(ByteBuffer src, int srcPos, ByteBuffer dest, int destPos, int length) {
		System.arraycopy(src.array(), srcPos, dest.array(), destPos, length);
	}

	public static double getDouble(byte[] bytes, int off) {
		return Double.longBitsToDouble(getLongBigEndian(bytes, off));
	}

	public static float getFloat(byte[] bytes, int off) {
		return Float.intBitsToFloat(getIntBigEndian(bytes, off));
	}

	public static int getIntBigEndian(byte[] bytes) {
		return getIntBigEndian(bytes, 0);
	}

	public static int getIntBigEndian(byte[] bytes, int off) {
		return //
		((bytes[off + 0] << 24) + //
				((bytes[off + 1] & 255) << 16) + //
				((bytes[off + 2] & 255) << 8) + //
		((bytes[off + 3] & 255) << 0));
	}

	public static int getIntLittleEndian(byte[] bytes) {
		return getIntLittleEndian(bytes, 0);
	}

	public static int getIntLittleEndian(byte[] bytes, int off) {
		return //
		((bytes[off + 3] << 24) + //
				((bytes[off + 2] & 255) << 16) + //
				((bytes[off + 1] & 255) << 8) + //
		((bytes[off + 0] & 255) << 0));
	}

	public static long getLongBigEndian(byte[] bytes) {
		return getLongBigEndian(bytes, 0);
	}

	public static long getLongBigEndian(byte[] bytes, int off) {
		return //
		(((long) bytes[off + 0] << 56) + //
				((long) (bytes[off + 1] & 255) << 48) + //
				((long) (bytes[off + 2] & 255) << 40) + //
				((long) (bytes[off + 3] & 255) << 32) + //
				((long) (bytes[off + 4] & 255) << 24) + //
				((bytes[off + 5] & 255) << 16) + //
				((bytes[off + 6] & 255) << 8) + //
		((bytes[off + 7] & 255) << 0)//
		);
	}

	public static long getLongLittleEndian(byte[] bytes) {
		return getLongLittleEndian(bytes, 0);
	}

	public static long getLongLittleEndian(byte[] bytes, int off) {
		return //
		(((long) bytes[off + 7] << 56) + //
				((long) (bytes[off + 6] & 255) << 48) + //
				((long) (bytes[off + 5] & 255) << 40) + //
				((long) (bytes[off + 4] & 255) << 32) + //
				((long) (bytes[off + 3] & 255) << 24) + //
				((bytes[off + 2] & 255) << 16) + //
				((bytes[off + 1] & 255) << 8) + //
		((bytes[off + 0] & 255) << 0)//
		);
	}

	public static int getRawVInt32(byte[] bytes, int off) {
		byte tmp = bytes[off++];
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = bytes[off++]) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = bytes[off++]) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = bytes[off++]) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = bytes[off++]) << 28;
					if (tmp < 0) {
						// 丢弃高位的5个字节，譬如遇到一个坑爹的writeVarint64(xx)
						for (int i = 0; i < 5; i++) {
							if (bytes[off++] >= 0) {
								return result;
							}
						}
						// 蛋疼
						throw new RuntimeException("Varint损坏");
					}
				}
			}
		}
		return result;
	}

	public static long getRawVInt64(byte[] bytes, int off) {
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final byte b = bytes[off++];
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		// 蛋疼
		throw new RuntimeException("Varint损坏");
	}

	public static int getSVInt32(byte[] bytes, int off) {
		return NeuronReader.uncrossInt32(getRawVInt32(bytes, off));
	}

	public static long getSVInt64(byte[] bytes, int off) {
		return NeuronReader.uncrossInt64(getRawVInt64(bytes, off));
	}

	public static void setByte(byte[] bytes, int off, int value) {
		bytes[off] = (byte) value;
	}

	public static void setIntBigEndian(int value, byte[] bytes, int off) {
		setByte(bytes, off++, (value >> 24) & 0xFF);
		setByte(bytes, off++, (value >> 16) & 0xFF);
		setByte(bytes, off++, (value >> 8) & 0xFF);
		setByte(bytes, off++, (value) & 0xFF);
	}

	public static void setIntLittleEndian(int value, byte[] bytes, int off) {
		setByte(bytes, off++, (value) & 0xFF);
		setByte(bytes, off++, (value >> 8) & 0xFF);
		setByte(bytes, off++, (value >> 16) & 0xFF);
		setByte(bytes, off++, (value >> 24) & 0xFF);
	}

	public static void setLongBigEndian(long value, byte[] bytes, int off) {
		setByte(bytes, off++, (int) (value >> 56) & 0xFF);
		setByte(bytes, off++, (int) (value >> 48) & 0xFF);
		setByte(bytes, off++, (int) (value >> 40) & 0xFF);
		setByte(bytes, off++, (int) (value >> 32) & 0xFF);
		setByte(bytes, off++, (int) (value >> 24) & 0xFF);
		setByte(bytes, off++, (int) (value >> 16) & 0xFF);
		setByte(bytes, off++, (int) (value >> 8) & 0xFF);
		setByte(bytes, off++, (int) (value) & 0xFF);
	}

	public static void setLongLittleEndian(long value, byte[] bytes, int off) {
		setByte(bytes, off++, (int) (value) & 0xFF);
		setByte(bytes, off++, (int) (value >> 8) & 0xFF);
		setByte(bytes, off++, (int) (value >> 16) & 0xFF);
		setByte(bytes, off++, (int) (value >> 24) & 0xFF);
		setByte(bytes, off++, (int) (value >> 32) & 0xFF);
		setByte(bytes, off++, (int) (value >> 40) & 0xFF);
		setByte(bytes, off++, (int) (value >> 48) & 0xFF);
		setByte(bytes, off++, (int) (value >> 56) & 0xFF);
	}

	public static String toMultiLineString(final byte[] bytes, Metrics metrics, int lineSize) {
		int len = bytes.length - 1;
		for (; len >= 0 && bytes[len] == 0; --len)
			;
		if (len == bytes.length - 1) {
			return ByteUtils.toString0(bytes, "0x", metrics, 0, len, lineSize).toString();
		}
		return ByteUtils.toString0(bytes, "0x", metrics, 0, len, lineSize).append(" + ").append(bytes.length - len + 1).append(" * 0x00").toString();
	}

	public static String toString(final byte[] bytes) {
		int len = bytes.length - 1;
		for (; len >= 0 && bytes[len] == 0; --len)
			;
		if (len == bytes.length - 1) {
			return toString0(bytes, "0x", null, 0, len, len).toString();
		}
		return toString0(bytes, "0x", null, 0, len, len).append(" + ").append(bytes.length - len + 1).append(" * 0x00").toString();
	}

	public static String toMultiLineString(final byte[] bytes, int lineSize) {
		int len = bytes.length - 1;
		for (; len >= 0 && bytes[len] == 0; --len)
			;
		if (len == bytes.length - 1) {
			return toString0(bytes, "0x", null, 0, len, lineSize).toString();
		}
		return toString0(bytes, "0x", null, 0, len, lineSize).append(" + ").append(bytes.length - len + 1).append(" * 0x00").toString();
	}

	public static String toString(final byte[] bytes, String prefix, int off, int len) {
		return toString0(bytes, prefix, null, off, len, len).toString();
	}

	public static String toMultiLineString(final byte[] bytes, String prefix, int off, int len, int lineSize) {
		return toString0(bytes, prefix, null, off, len, lineSize).toString();
	}

	static StringBuilder toString0(final byte[] bytes, String prefix, Metrics metrics, int off, int len, int lineLimit) {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");

		Iterator<Integer> iter = null;
		Integer offset = null;
		if (metrics != null) {
			iter = metrics.matricsMap.keySet().iterator();
			if (iter.hasNext()) {
				offset = iter.next();
			}
		}

		for (int i = off; i < off + len; ++i) {
			if (offset != null) {
				if (i == offset) {
					sb.append("\t[").append(metrics.matricsMap.get(offset)).append("]\t");
					if (iter.hasNext()) {
						offset = iter.next();
					} else {
						offset = null;
					}
				}
			}

			sb.append(prefix);
			sb.append(Integer.toHexString((bytes[i] & 0xff) >> 4).toUpperCase());
			sb.append(Integer.toHexString((bytes[i] & 0xff) % 16).toUpperCase());
			sb.append(" ");
			if ((i + 1 - off) % lineLimit == 0) {
				sb.append("]\n[ ");
			}
		}
		sb.append(']');
		return sb;
	}
}