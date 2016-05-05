package io.zeymo.commons.io;


import io.zeymo.commons.io.impl.NeuronByteArrayReader;
import io.zeymo.commons.io.impl.NeuronByteBufferReader;
import io.zeymo.commons.io.impl.NeuronFileChannelReader;
import io.zeymo.commons.io.impl.NeuronStreamReader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public abstract class NeuronReader {
	public final static Charset	DEFAULT_CHARSET	= Charset.forName("UTF-8"); //

	public final static int		BYTE_SIZE		= 1;
	public final static int		SHORT_SIZE		= 2;
	public final static int		INT_SIZE		= 4;
	public final static int		LONG_SIZE		= 8;

	public static NeuronByteArrayReader createReader(final byte[] src) {
		return NeuronReader.createReader(src, 0, src.length);
	}

	public static NeuronFileChannelReader createReader(FileChannel channel, final int bufferSize) {
		return new NeuronFileChannelReader(channel, bufferSize);
	}

	public static NeuronByteArrayReader createReader(final byte[] src, final int offset, final int length) {
		return new NeuronByteArrayReader(src, offset, length);
	}

	public static NeuronByteBufferReader createReader(final ByteBuffer buffer) {
		return new NeuronByteBufferReader(buffer);
	}

	public static NeuronStreamReader createReader(final InputStream input) {
		return new NeuronStreamReader(input);
	}

	public static int measureSignedVarint32(final int value) {
		return NeuronReader.measureVarint32(NeuronWriter.crossInt32(value));
	}

	public static int measureSignedVarint64(final long value) {
		return NeuronReader.measureVarint64(NeuronWriter.crossInt64(value));
	}

	public static int measureVarint32(int value) {
		if ((value & ~0x7F) == 0) {
			return 1;
		}
		value >>>= 7;
		if ((value & ~0x7F) == 0) {
			return 2;
		}
		value >>>= 7;
		if ((value & ~0x7F) == 0) {
			return 3;
		}
		value >>>= 7;
		if ((value & ~0x7F) == 0) {
			return 4;
		}
		return 5;
	}

	public static int measureVarint64(long value) {
		if ((value & ~0x7FL) == 0) {
			return 1;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 2;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 3;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 4;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 5;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 6;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 7;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 8;
		}
		value >>>= 7;
		if ((value & ~0x7FL) == 0) {
			return 9;
		}
		return 10;
	}

	public static int readRawInt8(final byte[] src, int offset) {
		final byte b1 = src[offset++];
		return (b1 & 0xff);
	}

	public static int readRawInt16(final byte[] src, int offset) {
		final byte b1 = src[offset++];
		final byte b2 = src[offset++];
		return ((b2 & 0xff)) | //
				((b1 & 0xff) << 8);

	}

	public static int readRawInt32(final byte[] src, int offset) {
		final byte b1 = src[offset++];
		final byte b2 = src[offset++];
		final byte b3 = src[offset++];
		final byte b4 = src[offset++];
		return ((b4 & 0xff)) | //
				((b3 & 0xff) << 8) | //
				((b2 & 0xff) << 16) | //
				((b1 & 0xff) << 24);

	}

	public static long readRawInt64(final byte[] src, int offset) {
		final byte b1 = src[offset++];
		final byte b2 = src[offset++];
		final byte b3 = src[offset++];
		final byte b4 = src[offset++];
		final byte b5 = src[offset++];
		final byte b6 = src[offset++];
		final byte b7 = src[offset++];
		final byte b8 = src[offset++];
		return (((long) b8 & 0xff)) | //
				(((long) b7 & 0xff) << 8) | //
				(((long) b6 & 0xff) << 16) | //
				(((long) b5 & 0xff) << 24) | //
				(((long) b4 & 0xff) << 32) | //
				(((long) b3 & 0xff) << 40) | //
				(((long) b2 & 0xff) << 48) | //
				(((long) b1 & 0xff) << 56);
	}

	public static int readRawSignedVarint32(final byte[] src, final int offset) throws IOException {
		return NeuronReader.uncrossInt32(NeuronReader.readRawVarint32(src, offset));
	}

	public static long readRawSignedVarint64(final byte[] src, final int offset) throws IOException {
		return NeuronReader.uncrossInt64(NeuronReader.readRawVarint64(src, offset));
	}

	public static int readRawVarint32(final byte[] src, int offset) throws IOException {
		byte tmp = src[offset++];
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = src[offset++]) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = src[offset++]) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = src[offset++]) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = src[offset++]) << 28;
					if (tmp < 0) {
						// 丢弃高位的5个字节，譬如遇到一个坑爹的writeVarint64(xx)
						for (int i = 0; i < 5; i++) {
							++offset;
							if (src[offset++] >= 0) {
								return result;
							}
						}
						// 蛋疼
						throw new IOException("Varint损坏");
					}
				}
			}
		}
		return result;
	}

	public static long readRawVarint64(final byte[] src, int offset) throws IOException {
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final byte b = src[offset++];
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		// 蛋疼
		throw new IOException("Varint损坏");
	}

	/**
	 * REUSE 相关的功能还是直接调用静态方法吧，不然用了二次派发还是疯狂地浪费性能<br />
	 * 注意原先的输入并不会被重用行为关闭
	 * 
	 * @param reader
	 * @param src
	 * @param offset
	 * @param length
	 * @return
	 */
	public static NeuronByteArrayReader reuse(final NeuronByteArrayReader reader, final byte[] src, final int offset, final int length) {
		reader.init(src, offset, length);
		return reader;
	}

	/**
	 * REUSE 相关的功能还是直接调用静态方法吧，不然用了二次派发还是疯狂地浪费性能<br />
	 * 注意原先的输入并不会被重用行为关闭
	 * 
	 * @param reader
	 * @param buffer
	 * @return
	 */
	public static NeuronByteBufferReader reuse(final NeuronByteBufferReader reader, final ByteBuffer buffer) {
		reader.init(buffer);
		return reader;
	}

	/**
	 * REUSE 相关的功能还是直接调用静态方法吧，不然用了二次派发还是疯狂地浪费性能<br />
	 * 注意原先的输入并不会被重用行为关闭
	 * 
	 * @param reader
	 * @param input
	 * @return
	 */
	public static NeuronStreamReader reuse(final NeuronStreamReader reader, final InputStream input) {
		reader.init(input);
		return reader;
	}

	/**
	 * 符号位放在最右边，同时其他位取消补码形态
	 * 
	 * @param n
	 * @return
	 */
	public static int uncrossInt32(final int n) {
		return (n >>> 1) ^ -(n & 1);
	}

	/**
	 * 符号位放在最右边，同时其他位取消补码形态
	 * 
	 * @param n
	 * @return
	 */
	public static long uncrossInt64(final long n) {
		return (n >>> 1) ^ -(n & 1);
	}

	public abstract void close() throws IOException;

	public abstract long getRelativeOffset();

	/**
	 * 从底层数据序列中读出一个 Boolean
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract boolean readBoolean() throws IOException;

	public abstract byte readByte() throws IOException;

	public abstract void readBytes(final byte[] dst, final int offset, final int length) throws IOException;

	public abstract void readBytes(final byte[] buffer) throws IOException;

	public abstract void readBytes(ByteBuffer buffer) throws IOException;

	public abstract void readBytes(ByteBuffer buffer, int length) throws IOException;

	/**
	 * 读出 size 长度的 byte[] ，接口保证每次返回新byte数组
	 * 
	 * @param size
	 * @return
	 * @throws IOException
	 */
	public abstract byte[] readBytes(final int size) throws IOException;

	public abstract int readShort() throws IOException;

	public abstract double readDouble() throws IOException;

	/**
	 * fixed int32
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract int readFInt() throws IOException;

	public abstract float readFloat() throws IOException;

	/**
	 * fixed int64
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract long readFLong() throws IOException;

	public abstract String readString() throws IOException;
	
	public abstract String readString(Charset charset) throws IOException;


	/**
	 * signed varint32
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract int readSVInt() throws IOException;

	/**
	 * signed Varint64
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract long readSVLong() throws IOException;

	/**
	 * unsigned varint32
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract int readUVInt() throws IOException;

	/**
	 * unsigned varint64
	 * 
	 * @return
	 * @throws IOException
	 */
	public abstract long readUVLong() throws IOException;

	public abstract void skip(int bytes) throws IOException;
}
