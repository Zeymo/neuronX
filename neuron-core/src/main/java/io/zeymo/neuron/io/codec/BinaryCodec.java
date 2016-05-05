package io.zeymo.neuron.io.codec;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronWriter;

import java.io.IOException;

public interface BinaryCodec {
	/**
	 * 返回当前域解析过程中从offset开始计算的长度，需要提供高性能实现
	 * 
	 * @param buffer
	 * @param offset
	 * @return
	 * @throws IOException
	 */
	public int decode(byte[] buffer, int offset) throws IOException;

	/**
	 * 用unionBuffer@typeIndex中的数据刷回writer，在形如docCollect阶段会用到
	 * 
	 * @param buffer
	 * @param typeIndex
	 * @param writer
	 * @throws IOException
	 */
	// public void encode(byte[] buffer, int typeIndex, MpWriter writer) throws IOException;

	/**
	 * 离线阶段构造数据体的方法，有点节操就行 如果data为NULL，请下游业务代码自行适配，主要考虑到空值不是普遍情况，为此浪费性能实在不靠谱
	 * 
	 * @param writer
	 * @throws IOException
	 */
	public void encode(NeuronInputBuffer inputBuffer, NeuronWriter writer) throws IOException;

	public Object debug(byte[] buffer);

}
