package io.zeymo.commons.io.impl;

import io.zeymo.commons.io.NeuronReader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public abstract class NeuronBaseReader extends NeuronReader {
	public static final String	EMPTY_STRING	= "";

	private byte[]				stringByteBuffer;

	@Override
	public boolean readBoolean() throws IOException {
		return this.readRawByte() != 0;
	}

	@Override
	public byte readByte() throws IOException {
		return this.readRawByte();
	}

	@Override
	public void readBytes(final byte[] buffer, final int offset, final int length) throws IOException {
		this.readRawBytes(buffer, offset, length);
	}

	@Override
	public void readBytes(final byte[] buffer) throws IOException {
		this.readRawBytes(buffer, 0, buffer.length);
	}

	@Override
	public void readBytes(final ByteBuffer byteBuffer) throws IOException {
		this.readRawBytes(byteBuffer, byteBuffer.remaining());
	}

	@Override
	public void readBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		this.readRawBytes(byteBuffer, length);
	}

	@Override
	public byte[] readBytes(final int size) throws IOException {
		final byte[] bytes = new byte[size];
		this.readRawBytes(bytes, 0, size);
		return bytes;
	}

	@Override
	public int readShort() throws IOException {
		return this.readFIntBigEndian16();
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(this.readFIntBigEndian64());
	}

	@Override
	public int readFInt() throws IOException {
		return this.readFIntBigEndian32();
	}

	public int readFIntBigEndian16() throws IOException {
		final byte b1 = this.readRawByte();
		final byte b2 = this.readRawByte();
		return (b2 & 0xff) | //
				((b1 & 0xff) << 8);
	}

	public int readFIntBigEndian32() throws IOException {
		final byte b1 = this.readRawByte();
		final byte b2 = this.readRawByte();
		final byte b3 = this.readRawByte();
		final byte b4 = this.readRawByte();
		return ((b4 & 0xff)) | //
				((b3 & 0xff) << 8) | //
				((b2 & 0xff) << 16) | //
				((b1 & 0xff) << 24);
	}

	public long readFIntBigEndian64() throws IOException {
		final byte b1 = this.readRawByte();
		final byte b2 = this.readRawByte();
		final byte b3 = this.readRawByte();
		final byte b4 = this.readRawByte();
		final byte b5 = this.readRawByte();
		final byte b6 = this.readRawByte();
		final byte b7 = this.readRawByte();
		final byte b8 = this.readRawByte();
		return (((long) b8 & 0xff)) | //
				(((long) b7 & 0xff) << 8) | //
				(((long) b6 & 0xff) << 16) | //
				(((long) b5 & 0xff) << 24) | //
				(((long) b4 & 0xff) << 32) | //
				(((long) b3 & 0xff) << 40) | //
				(((long) b2 & 0xff) << 48) | //
				(((long) b1 & 0xff) << 56);
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(this.readFIntBigEndian32());
	}

	@Override
	public long readFLong() throws IOException {
		return this.readFIntBigEndian64();
	}

	public abstract byte readRawByte() throws IOException;

	public abstract void readRawBytes(byte[] dst, int offset, int length) throws IOException;

	public abstract void readRawBytes(ByteBuffer byteBuffer, int length) throws IOException;

	public int readRawVarint32() throws IOException {
		int tmp = this.readRawByte();
		if (tmp >= 0) {
			return tmp;
		}
		int result = tmp & 0x7f;
		if ((tmp = this.readRawByte()) >= 0) {
			result |= tmp << 7;
		} else {
			result |= (tmp & 0x7f) << 7;
			if ((tmp = this.readRawByte()) >= 0) {
				result |= tmp << 14;
			} else {
				result |= (tmp & 0x7f) << 14;
				if ((tmp = this.readRawByte()) >= 0) {
					result |= tmp << 21;
				} else {
					result |= (tmp & 0x7f) << 21;
					result |= (tmp = this.readRawByte()) << 28;
					if (tmp < 0) {
						// 丢弃高位的5个字节，譬如遇到一个坑爹的writeVarint64(xx)
						for (int i = 0; i < 5; i++) {
							if (this.readRawByte() >= 0) {
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

	public long readRawVarint64() throws IOException {
		int shift = 0;
		long result = 0;
		while (shift < 64) {
			final byte b = this.readRawByte();
			result |= (long) (b & 0x7F) << shift;
			if ((b & 0x80) == 0) {
				return result;
			}
			shift += 7;
		}
		// 蛋疼
		throw new IOException("Varint损坏");
	}

	@Override
	public String readString() throws IOException {
		return readString(NeuronReader.DEFAULT_CHARSET);
	}

	@Override
	public String readString(Charset charset) throws IOException {
		final int size = this.readRawVarint32();
		if (size == 0) {
			return NeuronBaseReader.EMPTY_STRING;
		}
		if ((this.stringByteBuffer == null) || (this.stringByteBuffer.length < size)) {
			this.stringByteBuffer = new byte[size];
		}
		this.readRawBytes(this.stringByteBuffer, 0, size);
		return new String(this.stringByteBuffer, 0, size, charset);
	}

	@Override
	public int readSVInt() throws IOException {
		return NeuronReader.uncrossInt32(this.readRawVarint32());
	}

	@Override
	public long readSVLong() throws IOException {
		return NeuronReader.uncrossInt64(this.readRawVarint64());
	}

	@Override
	public int readUVInt() throws IOException {
		return this.readRawVarint32();
	}

	@Override
	public long readUVLong() throws IOException {
		return this.readRawVarint64();
	}

}
