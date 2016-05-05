package io.zeymo.neuron.io;

import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.domain.Varchar;
import io.zeymo.neuron.schema.NodeLayout;

/**
 * 用int[]替代object[]对基本类型共享内存提供读写<br />
 * int[]对比object{int val}的性能测试结果大概是5倍差距<br />
 * （主要问题在于超出Lx缓存，单拉UCache实现对比，大概比对象域性能高30%左右），还是相当明显<br />
 * <br />
 * byte/short/char/boolean这种没意思的数据类型就没必要实现了吧
 */
public class UnionBuffer {

	public static final int getIndex(final int fieldIndex) {
		return fieldIndex * NeuronConstants.UNION_CACHE_UNIT_SIZE;
	}

	private final int[]			cache;
	private final SerializeMap	sidMap;

	public UnionBuffer(int size, NodeLayout nodeLayout) {
		this.cache = new int[size * NeuronConstants.UNION_CACHE_UNIT_SIZE];
		this.sidMap = new SerializeMap(nodeLayout);
	}

	public UnionBuffer(NodeLayout nodeLayout) {
		this.cache = new int[NeuronConstants.RUNTIME_MAX_FIELD_COUNT * NeuronConstants.UNION_CACHE_UNIT_SIZE];
		this.sidMap = new SerializeMap(nodeLayout);

	}

	public double getDouble(int fieldIndex) {
		long rawInt64 = getLong(fieldIndex);
		return Double.longBitsToDouble(rawInt64);
	}

	public double getDouble(int sectorIndex, int fieldIndex) {
		fieldIndex = sidMap.getFieldIndex(sectorIndex, fieldIndex);
		return getDouble(fieldIndex);
	}

	public float getFloat(int fieldIndex) {
		int rawInt32 = getInt(fieldIndex);
		return Float.intBitsToFloat(rawInt32);
	}

	public float getFloat(int sectorIndex, int fieldIndex) {
		fieldIndex = sidMap.getFieldIndex(sectorIndex, fieldIndex);
		return getFloat(fieldIndex);
	}

	public int getInt(int fieldIndex) {
		int index = getIndex(fieldIndex);
		return cache[index];
	}

	public int getInt(int sectorIndex, int fieldIndex) {
		fieldIndex = sidMap.getFieldIndex(sectorIndex, fieldIndex);
		return getInt(fieldIndex);
	}

	public long getLong(int fieldIndex) {
		int index = getIndex(fieldIndex);
		long value = ((long) cache[index]) << 32;
		value |= (cache[++index]) & 0xFFFFFFFFL;
		return value;
	}

	public long getLong(int sectorIndex, int fieldIndex) {
		fieldIndex = sidMap.getFieldIndex(sectorIndex, fieldIndex);
		return getLong(fieldIndex);
	}

	public int getSubInt(int fieldIndex, int subIndex) {
		int index = getIndex(fieldIndex) + subIndex;
		return cache[index];
	}

	public int getSubInt(int sectorIndex, int fieldIndex, int subIndex) {
		fieldIndex = sidMap.getFieldIndex(sectorIndex, fieldIndex);
		return getSubInt(fieldIndex, subIndex);
	}

	public void getVarchar(int fieldIndex, byte[] rawBuffer, Varchar varchar) {
		int off = getSubInt(fieldIndex, 0);
		int len = getSubInt(fieldIndex, 1);

		varchar.set(rawBuffer, off, len);
	}

	public void setDouble(int fieldIndex, double value) {
		long rawInt64 = Double.doubleToLongBits(value);
		setLong(fieldIndex, rawInt64);
	}

	public void setFloat(int fieldIndex, float value) {
		int rawInt32 = Float.floatToIntBits(value);
		this.setInt(fieldIndex, rawInt32);
	}

	public void setInt(int fieldIndex, int value) {
		int index = getIndex(fieldIndex);
		cache[index] = value;
	}

	public void setSubInt(int fieldIndex, int subIndex, int value) {
		int index = getIndex(fieldIndex) + subIndex;
		cache[index] = value;
	}

	public void setLong(int fieldIndex, long value) {
		int high = ((int) (value >> 32));
		int low = ((int) value);

		int index = getIndex(fieldIndex);
		cache[index] = high;
		cache[++index] = low;
	}

}
