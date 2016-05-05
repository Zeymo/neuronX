package io.zeymo.commons.collections;

public class PrimitiveDoubleArray {
	private final double[]	array;
	private final int		size;
	private int				count;

	public PrimitiveDoubleArray(int size) {
		this.array = new double[size];
		this.size = size;
	}

	public double get(int index) {
		return array[index];
	}

	public void add(double value) {
		this.array[count++] = value;
	}

	public int size() {
		return size;
	}

	public int count() {
		return count;
	}

	public double[] array() {
		return array;
	}

	public void clear() {
		this.count = 0;
	}
}
