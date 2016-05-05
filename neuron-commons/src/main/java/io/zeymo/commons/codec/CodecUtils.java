package io.zeymo.commons.codec;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CodecUtils {

	private static char[]		B64_ALPHABET		= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".toCharArray();
	private static byte[]		B64_CODES			= new byte[256];

	public static final Charset	CHARSET				= Charset.forName("UTF-8");
	public static final int		COMPRESS_THRESHOLD	= 256;

	static {
		for (int i = 0; i < 256; i++)
			B64_CODES[i] = -1;
		for (int i = 'A'; i <= 'Z'; i++)
			B64_CODES[i] = (byte) (i - 'A');
		for (int i = 'a'; i <= 'z'; i++)
			B64_CODES[i] = (byte) (26 + i - 'a');
		for (int i = '0'; i <= '9'; i++)
			B64_CODES[i] = (byte) (52 + i - '0');
		B64_CODES['+'] = 62;
		B64_CODES['/'] = 63;
	}

	public static byte[] decodeBase64(char[] data) {
		int len = ((data.length + 3) / 4) * 3;
		if (data.length > 0 && data[data.length - 1] == '=')
			--len;
		if (data.length > 1 && data[data.length - 2] == '=')
			--len;
		byte[] out = new byte[len];
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < data.length; ix++) {
			int value = B64_CODES[data[ix] & 0xFF];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) ((accum >> shift) & 0xff);
				}
			}
		}
		if (index != out.length)
			throw new RuntimeException("miscalculated data length!");
		return out;
	}

	public static byte[] decodeBase64String(String string) {
		return decodeBase64(string.toCharArray());
	}

	public static char[] encodeBase64(byte[] data) {
		char[] out = new char[((data.length + 2) / 3) * 4];

		for (int i = 0, index = 0; i < data.length; i += 3, index += 4) {
			boolean quad = false;
			boolean trip = false;
			int val = (0xFF & data[i]);
			val <<= 8;
			if ((i + 1) < data.length) {
				val |= (0xFF & data[i + 1]);
				trip = true;
			}
			val <<= 8;
			if ((i + 2) < data.length) {
				val |= (0xFF & data[i + 2]);
				quad = true;
			}
			out[index + 3] = B64_ALPHABET[(quad ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 2] = B64_ALPHABET[(trip ? (val & 0x3F) : 64)];
			val >>= 6;
			out[index + 1] = B64_ALPHABET[val & 0x3F];
			val >>= 6;
			out[index + 0] = B64_ALPHABET[val & 0x3F];
		}
		return out;
	}

	public static String encodeBase64String(byte[] data) {
		return new String(encodeBase64(data));
	}

	public static String getCompressedBase64String(byte[] rawBytes) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		byte[] outputBytes = rawBytes;

		int compressed = 0;
		if (rawBytes.length > COMPRESS_THRESHOLD) {
			byte[] compressedBytes = getCompressedBytes(rawBytes, 0, rawBytes.length);
			if (compressedBytes.length < rawBytes.length) {
				compressed = 1;
				outputBytes = compressedBytes;
			}
		}
		baos.write(compressed);
		baos.write(outputBytes);

		char[] base64Chars = encodeBase64(baos.toByteArray());
		return new String(base64Chars);
	}

	public static String getCompressedBase64String(Object object) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(object);
		oos.close();
		byte[] array = baos.toByteArray();
		return getCompressedBase64String(array);
	}

	public static String getCompressedBase64String(String rawString) throws IOException {
		return packToBase64String(rawString);
	}

	public static byte[] getCompressedBytes(byte[] rawBytes, int off, int len) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		GZIPOutputStream gz = new GZIPOutputStream(baos);
		gz.write(rawBytes, off, len);
		gz.finish();
		gz.close();
		byte[] compressedBytes = baos.toByteArray();
		return compressedBytes;
	}

	public static byte[] getCompressedBytes(String src) throws IOException {
		byte[] rawBytes = src.getBytes(CHARSET);
		return getCompressedBytes(rawBytes, 0, rawBytes.length);
	}

	public static byte[] getDecompressedBytesFromBase64(String base64) throws IOException {
		char[] base64Chars = base64.toCharArray();
		byte[] inputBytes = decodeBase64(base64Chars);

		int compressed = inputBytes[0];
		if (compressed == 1) {
			byte[] rawBytes = getDeompressedBytes(inputBytes, 1, inputBytes.length - 1);
			return rawBytes;
		} else {
			return Arrays.copyOfRange(inputBytes, 1, inputBytes.length);
		}

	}

	public static String getDecompressedStringFromBase64(String compressedString) throws IOException {
		return unpackFromBase64String(compressedString);
	}

	public static byte[] getDeompressedBytes(byte[] compressedBytes, int off, int len) throws IOException {
		GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(compressedBytes, off, len));
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		byte[] buffer = new byte[1024];
		int length = gz.read(buffer);

		while (length > 0) {
			baos.write(buffer, 0, length);
			length = gz.read(buffer);
		}

		byte[] rawBytes = baos.toByteArray();
		return rawBytes;
	}

	public static Object getDecompressedObjectFromBase64(String compressedString) throws IOException, ClassNotFoundException {
		byte[] data = getDecompressedBytesFromBase64(compressedString);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();
		return object;
	}

	/**
	 * 将一个字符串压缩后存储为base64
	 * 
	 * @param rawString
	 * @return
	 * @throws IOException
	 */
	public static String packToBase64String(String rawString) throws IOException {

		byte[] rawBytes = rawString.getBytes(CHARSET);
		return getCompressedBase64String(rawBytes);
	}

	/**
	 * 解压一个被base64压缩过的字符串
	 * 
	 * @param compressedString
	 * @return
	 * @throws IOException
	 */
	public static String unpackFromBase64String(String compressedString) throws IOException {
		char[] base64Chars = compressedString.toCharArray();
		byte[] inputBytes = decodeBase64(base64Chars);
		String rawString = null;

		int compressed = inputBytes[0];
		if (compressed == 1) {
			byte[] rawBytes = getDeompressedBytes(inputBytes, 1, inputBytes.length - 1);
			rawString = new String(rawBytes, CHARSET);
		} else {
			rawString = new String(inputBytes, 1, inputBytes.length - 1, CHARSET);
		}
		return rawString;
	}
}
