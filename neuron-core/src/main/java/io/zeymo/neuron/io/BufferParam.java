package io.zeymo.neuron.io;

import io.zeymo.commons.io.NeuronInputBuffer;

import java.nio.ByteBuffer;

/**
 * 
 * 不直接包装ByteBuffer的原因主要如下：
 * <ul>
 * <li>ByteBuffer内部维护指针，对于MpReader而言无需维护</li>
 * <li>如果使用ByteBuffer，会多出不少内存拷贝操作，或者推心置腹的内部变量get</li>
 * <li>仅有Storage读出时需要将ByteBuffer转换到byte []</li>
 * </ul>
 * 
 * @author fudi
 * @since 2014
 * 
 */
public class BufferParam {
	private final byte[]		buffer;

	private final ByteBuffer	byteBuffer;

	private int					length;

	public void set(NeuronInputBuffer inputBuffer, int offset, int length) {
		System.arraycopy(inputBuffer.getBuffer(), offset, this.buffer, 0, length);
		this.length = length;
	}

	public void set(byte[] buffer, int offset, int length) {
		System.arraycopy(buffer, offset, this.buffer, 0, length);
		this.length = length;
	}

	public BufferParam(final byte[] buffer) {
		this(buffer, buffer.length);
	}

	public BufferParam(final byte[] buffer, int length) {
		this.buffer = buffer;
		this.length = length;
		this.byteBuffer = ByteBuffer.wrap(buffer);
	}

	public void replicate(BufferParam param) {
		this.length = param.getLength();
		System.arraycopy(param.getBuffer(), 0, buffer, 0, length);
	}

	public BufferParam(int size) {
		this(new byte[size], size);
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public ByteBuffer getByteBuffer(int length) {
		this.byteBuffer.position(0).limit(length);
		this.length = length;
		return byteBuffer;
	}

	public ByteBuffer getByteBuffer() {
		this.byteBuffer.position(0).limit(this.length);
		return byteBuffer;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

}
