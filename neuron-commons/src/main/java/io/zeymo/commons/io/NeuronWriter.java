package io.zeymo.commons.io;


import io.zeymo.commons.io.impl.NeuronByteArrayWriter;
import io.zeymo.commons.io.impl.NeuronByteBufferWriter;
import io.zeymo.commons.io.impl.NeuronFileStreamWriter;
import io.zeymo.commons.io.impl.NeuronStreamWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class NeuronWriter {
	public static final Charset	DEFAULT_CHARSET	= Charset.forName("UTF-8"); //

	public static NeuronWriter createWriter(final OutputStream output) {
		return new NeuronStreamWriter(output);
	}

	public static NeuronWriter createWriter(final byte[] dst) {
		return NeuronWriter.createWriter(dst, 0, dst.length);
	}

	public static NeuronWriter createWriter(final byte[] dst, final int offset, final int length) {
		return new NeuronByteArrayWriter(dst, offset, length);
	}

	public static NeuronWriter createWriter(final ByteBuffer buffer) {
		return new NeuronByteBufferWriter(buffer);
	}

	public static NeuronWriter createWriter(final FileOutputStream output) {
		return new NeuronFileStreamWriter(output);
	}

	/**
	 * 符号位扔到最右面
	 * 
	 * @param n
	 * @return
	 */
	public static int crossInt32(final int n) {
		return (n << 1) ^ (n >> 31);
	}

	/**
	 * 符号位扔到最右面
	 * 
	 * @param n
	 * @return
	 */
	public static long crossInt64(final long n) {
		return (n << 1) ^ (n >> 63);
	}

	public static NeuronByteArrayWriter reuse(final NeuronByteArrayWriter writer, final byte[] src) {
		writer.init(src, 0, src.length);
		return writer;
	}

	public static NeuronByteArrayWriter reuse(final NeuronByteArrayWriter writer, final byte[] src, final int offset, final int length) {
		writer.init(src, offset, length);
		return writer;
	}

	public static void writeDouble(final byte[] buffer, final int offset, final double value) {
		NeuronWriter.writeRawBigEndian64(buffer, offset, Double.doubleToRawLongBits(value));
	}

	public static void writeFInt(final byte[] buffer, final int offset, final int value) {
		NeuronWriter.writeRawBigEndian32(buffer, offset, value);

	}

	public static void writeFLong(final byte[] buffer, final int offset, final long value) {
		NeuronWriter.writeRawBigEndian64(buffer, offset, value);
	}

	public static void writeRawBigEndian32(final byte[] buffer, int offset, final int value) {
		buffer[offset++] = (byte) (value >> 24);
		buffer[offset++] = (byte) (value >> 16);
		buffer[offset++] = (byte) (value >> 8);
		buffer[offset++] = (byte) (value);
	}

	public static void writeRawBigEndian64(final byte[] buffer, int offset, final long value) {
		buffer[offset++] = (byte) (value >> 56);
		buffer[offset++] = (byte) (value >> 48);
		buffer[offset++] = (byte) (value >> 40);
		buffer[offset++] = (byte) (value >> 32);
		buffer[offset++] = (byte) (value >> 24);
		buffer[offset++] = (byte) (value >> 16);
		buffer[offset++] = (byte) (value >> 8);
		buffer[offset++] = (byte) (value);
	}

	public abstract void close() throws IOException;

	public abstract void flush() throws IOException;

	public abstract void writeBoolean(boolean value) throws IOException;

	public abstract void writeByte(int value) throws IOException;

	/**
	 * 这个方法绝对不会自作多情的帮用户记录value.length
	 * 
	 * @throws IOException
	 */
	public abstract void writeBytes(byte[] src) throws IOException;

	/**
	 * 这个方法也绝对不会记录values.length
	 * 
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract void writeBytes(byte[] src, int offset, int length) throws IOException;

	public abstract void writeBytes(ByteBuffer buffer) throws IOException;

	public abstract void writeBytes(ByteBuffer buffer, int length) throws IOException;

	public abstract void writeShort(int value) throws IOException;

	public abstract void writeDouble(double value) throws IOException;

	public abstract int getRelativeOffset();

	/**
	 * fixed int32
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeFInt(int value) throws IOException;

	public abstract void writeFloat(float value) throws IOException;

	public void writeFloat(final float value, final byte[] buffer, final int offset) {
		NeuronWriter.writeRawBigEndian32(buffer, offset, Float.floatToRawIntBits(value));
	}

	/**
	 * fixed int64
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeFLong(long value) throws IOException;

	/**
	 * 写入一个String，意味着这个方法会记录value.length
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeString(String value) throws IOException;

	/**
	 * signed varint32
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeSVInt(int value) throws IOException;

	/**
	 * signed varint64
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeSVLong(long value) throws IOException;

	/**
	 * unsigned varint32
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeUVInt(int value) throws IOException;

	/**
	 * unsigned varint64
	 * 
	 * @param value
	 * @throws IOException
	 */
	public abstract void writeUVLong(long value) throws IOException;
}