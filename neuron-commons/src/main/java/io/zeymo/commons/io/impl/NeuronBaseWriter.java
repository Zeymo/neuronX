package io.zeymo.commons.io.impl;


import io.zeymo.commons.io.NeuronWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

public abstract class NeuronBaseWriter extends NeuronWriter {

	/**
	 * 虽然从性能角度考虑在flush()方法里面调用output的flush不太厚道，但为了避免出现各种误用，还是加上了
	 * 
	 * @throws IOException
	 */
	@Override
	public abstract void flush() throws IOException;

	@Override
	public void writeBoolean(final boolean value) throws IOException {
		this.writeRawByte(value ? 1 : 0);
	}

	@Override
	public void writeByte(final int value) throws IOException {
		this.writeRawByte(value);

	}

	@Override
	public void writeBytes(final byte[] value) throws IOException {
		this.writeRawBytes(value);
	}

	@Override
	public void writeBytes(final byte[] value, final int offset, final int length) throws IOException {
		this.writeRawBytes(value, offset, length);
	}

	@Override
	public void writeBytes(final ByteBuffer byteBuffer) throws IOException {
		this.writeRawBytes(byteBuffer, byteBuffer.remaining());
	}

	@Override
	public void writeBytes(final ByteBuffer byteBuffer, final int length) throws IOException {
		this.writeRawBytes(byteBuffer, length);
	}

	@Override
	public void writeShort(final int value) throws IOException {
		this.writeRawFixedInt16(value);
	}

	@Override
	public void writeDouble(final double value) throws IOException {
		this.writeRawBigEndian64(Double.doubleToRawLongBits(value));
	}

	@Override
	public void writeFInt(final int value) throws IOException {
		this.writeRawBigEndian32(value);

	}

	@Override
	public void writeFloat(final float value) throws IOException {
		this.writeRawBigEndian32(Float.floatToRawIntBits(value));
	}

	@Override
	public void writeFLong(final long value) throws IOException {
		this.writeRawBigEndian64(value);
	}

	public void writeRawBigEndian16(final int value) throws IOException {
		this.writeRawByte((value >> 8) & 0xFF);
		this.writeRawByte((value) & 0xFF);
	}

	public void writeRawBigEndian32(final int value) throws IOException {
		this.writeRawByte((value >> 24) & 0xFF);
		this.writeRawByte((value >> 16) & 0xFF);
		this.writeRawByte((value >> 8) & 0xFF);
		this.writeRawByte((value) & 0xFF);
	}

	public void writeRawBigEndian64(final long value) throws IOException {
		this.writeRawByte((int) (value >> 56) & 0xFF);
		this.writeRawByte((int) (value >> 48) & 0xFF);
		this.writeRawByte((int) (value >> 40) & 0xFF);
		this.writeRawByte((int) (value >> 32) & 0xFF);
		this.writeRawByte((int) (value >> 24) & 0xFF);
		this.writeRawByte((int) (value >> 16) & 0xFF);
		this.writeRawByte((int) (value >> 8) & 0xFF);
		this.writeRawByte((int) (value) & 0xFF);
	}

	public abstract void writeRawByte(int b) throws IOException;

	public void writeRawBytes(final byte src[]) throws IOException {
		this.writeRawBytes(src, 0, src.length);
	}

	/**
	 * 直接在byte[]上写入数据，这种方法很适合SST的Payload使用
	 * 
	 * @param offset
	 * @param length
	 * @throws IOException
	 */
	public abstract void writeRawBytes(final byte[] src, int offset, int length) throws IOException;

	public abstract void writeRawBytes(ByteBuffer byteBuffer, int length) throws IOException;

	public void writeRawFixedInt16(final int value) throws IOException {
		this.writeRawBigEndian16(value);
	}

	public void writeRawFixedInt32(final int value) throws IOException {
		this.writeRawBigEndian32(value);
	}

	public void writeRawFixedInt64(final long value) throws IOException {
		this.writeRawBigEndian64(value);
	}

	public void writeRawLittleEndian32(final int value) throws IOException {
		this.writeRawByte((value) & 0xFF);
		this.writeRawByte((value >> 8) & 0xFF);
		this.writeRawByte((value >> 16) & 0xFF);
		this.writeRawByte((value >> 24) & 0xFF);
	}

	public void writeRawLittleEndian64(final long value) throws IOException {
		this.writeRawByte((int) (value) & 0xFF);
		this.writeRawByte((int) (value >> 8) & 0xFF);
		this.writeRawByte((int) (value >> 16) & 0xFF);
		this.writeRawByte((int) (value >> 24) & 0xFF);
		this.writeRawByte((int) (value >> 32) & 0xFF);
		this.writeRawByte((int) (value >> 40) & 0xFF);
		this.writeRawByte((int) (value >> 48) & 0xFF);
		this.writeRawByte((int) (value >> 56) & 0xFF);
	}

	public void writeRawSignedVarint32(final int value) throws IOException {
		this.writeRawVarint32(NeuronWriter.crossInt32(value));
	}

	public void writeRawSignedVarint64(final int value) throws IOException {
		this.writeRawVarint64(NeuronWriter.crossInt64(value));
	}

	public void writeRawVarint32(int value) throws IOException {
		while (true) {
			if ((value & ~0x7F) == 0) {
				this.writeRawByte(value);
				return;
			} else {
				this.writeRawByte((value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	public void writeRawVarint64(long value) throws IOException {
		while (true) {
			if ((value & ~0x7FL) == 0) {
				this.writeRawByte((int) value);
				return;
			} else {
				this.writeRawByte(((int) value & 0x7F) | 0x80);
				value >>>= 7;
			}
		}
	}

	@Override
	public void writeString(final String value) throws IOException {
		if (value == null) {
			this.writeRawVarint32(0);
			return;
		}
		final byte[] bytes = value.getBytes(NeuronWriter.DEFAULT_CHARSET);
		this.writeRawVarint32(bytes.length);
		this.writeRawBytes(bytes);
	}

	/**
	 * 默认作为有符号的int
	 * 
	 * @param value
	 * @throws IOException
	 */
	@Override
	public void writeSVInt(final int value) throws IOException {
		this.writeRawVarint32(NeuronWriter.crossInt32(value));
	}

	/**
	 * 默认作为有符号的long
	 * 
	 * @param value
	 * @throws IOException
	 */
	@Override
	public void writeSVLong(final long value) throws IOException {
		this.writeRawVarint64(NeuronWriter.crossInt64(value));
	}

	@Override
	public void writeUVInt(final int value) throws IOException {
		this.writeRawVarint32(value);
	}

	@Override
	public void writeUVLong(final long value) throws IOException {
		this.writeRawVarint64(value);
	}
}
