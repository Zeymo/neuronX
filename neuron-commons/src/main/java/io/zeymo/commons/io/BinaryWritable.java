package io.zeymo.commons.io;

import java.io.IOException;

public interface BinaryWritable {
	public static final BinaryWritable	NULL	= new Null();

	public static class Null implements BinaryWritable {

		@Override
		public void readFields(NeuronReader reader) throws IOException {
		}

		@Override
		public void write(NeuronWriter writer) throws IOException {
		}
	}

	public void readFields(NeuronReader reader) throws IOException;

	public void write(NeuronWriter writer) throws IOException;

	/**
	 * 
	 * 不要在主体还没完成初始化的时候就开始把reader交给下面做reuse，不然死很惨
	 * 
	 * @param reusableReader
	 * @param buffer
	 * @param offset
	 * @param length
	 */
	// public int reset(MpByteArrayReader reusableReader, byte[] buffer, int
	// offset, int length) throws IOException;
}
