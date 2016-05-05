package io.zeymo.neuron.domain;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.NeuronConstants;

import java.io.IOException;

public class Varchar implements BinaryWritable {
	public static int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
		int end1 = s1 + l1;
		int end2 = s2 + l2;
		for (int i = s1, j = s2; i < end1 && j < end2; i++, j++) {
			int a = (b1[i] & 0xff);
			int b = (b2[j] & 0xff);
			if (a != b) {
				return a - b;
			}
		}
		return l1 - l2;
	}

	public static int compareReader(NeuronReader reader1, NeuronReader reader2) throws IOException {
		int length1 = reader1.readUVInt();
		int length2 = reader2.readUVInt();
		// int targetPos1 = (int) reader1.getRelativeOffset() + length1;
		// int targetPos2 = (int) reader2.getRelativeOffset() + length2;

		int min = length1 < length2 ? length1 : length2;
		for (int i = 0; i < min; i++) {
			int a = (reader1.readByte() & 0xff);
			int b = (reader2.readByte() & 0xff);
			if (a != b) {
				return a - b;
			}
		}
		// TODO FIX THIS !!!!!!!!!!!!!!!!!!!
		// reader1.seek(targetPos1);
		// reader2.seek(targetPos2);
		return length1 - length2;
	}

	private byte[]	bytes;
	private int		hash;
	private int		length;

	public Varchar(int length) {
		this(new byte[length]);
	}

	public Varchar(byte[] utf8) {
		this(utf8, 0, utf8.length);
	}

	public Varchar(byte[] utf8, int start, int len) {
		setCapacity(len, false);
		System.arraycopy(utf8, start, bytes, 0, len);
		this.length = len;
	}

	public Varchar(String s) {
		bytes = s.getBytes(NeuronConstants.STRING_CHARSET);
		length = bytes.length;

	}

	public int compareTo(Varchar another) {
		return compareBytes(bytes, 0, length, another.bytes, 0, another.length);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Varchar)) {
			return false;
		}

		Varchar varchar = (Varchar) o;

		if (varchar.length != length) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			if (bytes[i] != varchar.bytes[i]) {
				return false;
			}
		}
		return true;
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int getLength() {
		return length;
	}

	@Override
	public int hashCode() {
		int h = hash;
		if (h == 0 && length > 0) {
			for (int i = 0; i < length; i++) {
				h = 31 * h + bytes[i];
			}
			hash = h;
		}
		return h;
	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {
		this.length = reader.readUVInt();
		reader.readBytes(bytes, 0, length);
		this.hash = 0;
	}

	public void set(byte[] source) {
		set(source, 0, source.length);
	}

	public void set(byte[] source, int start, int len) {
		setCapacity(len, false);
		System.arraycopy(source, start, bytes, 0, len);
		this.length = len;
		this.hash = 0;
	}

	public void clear() {
		this.length = 0;
	}

	public void set(String string) {
		byte[] b = string.getBytes(NeuronConstants.STRING_CHARSET);
		length = b.length;

		for (int i = 0; i < length; ++i) {
			this.bytes[i] = b[i];
		}
		this.hash = 0;
	}

	public void set(Varchar varchar) {
		this.set(varchar.bytes, 0, varchar.length);
	}

	private void setCapacity(int len, boolean keepData) {
		if (bytes == null || bytes.length < len) {
			byte[] newBytes = new byte[len];
			if (bytes != null && keepData) {
				System.arraycopy(bytes, 0, newBytes, 0, length);
			}
			bytes = newBytes;
		}
	}

	@Override
	public String toString() {
		return new String(bytes, 0, length, NeuronConstants.STRING_CHARSET);
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		writer.writeUVInt(this.length);
		writer.writeBytes(bytes, 0, length);

	}

}
