package io.zeymo.commons.collections;

public class PrimitiveIntArray {
	private final int[]	array;
	private final int	size;
	private int			count;

	public PrimitiveIntArray(int size) {
		this.array = new int[size];
		this.size = size;
	}

	public int get(int index) {
		return array[index];
	}

	public void add(int value) {
		this.array[count++] = value;
	}

	public int size() {
		return size;
	}

	public int count() {
		return count;
	}

	public int[] array() {
		return array;
	}

	public void clear() {
		this.count = 0;
	}
}
