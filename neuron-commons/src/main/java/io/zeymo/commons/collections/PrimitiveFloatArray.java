package io.zeymo.commons.collections;

public class PrimitiveFloatArray {
	private final float[]	array;
	private final int		size;
	private int				count;

	public PrimitiveFloatArray(int size) {
		this.array = new float[size];
		this.size = size;
	}

	public float get(int index) {
		return array[index];
	}

	public void add(float value) {
		this.array[count++] = value;
	}

	public int size() {
		return size;
	}

	public float[] array() {
		return array;
	}

	public int count() {
		return count;
	}

	public void clear() {
		this.count = 0;
	}
}
