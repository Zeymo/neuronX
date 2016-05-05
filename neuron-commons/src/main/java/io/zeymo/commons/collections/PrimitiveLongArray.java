package io.zeymo.commons.collections;

public class PrimitiveLongArray {
	private final long[]	array;
	private final int		size;
	private int				count;

	public PrimitiveLongArray(int size) {
		this.array = new long[size];
		this.size = size;
	}

	public long get(int index) {
		return array[index];
	}

	public void add(long value) {
		this.array[count++] = value;
	}
	
	public int count(){
		return count;
	}
	
	public int size(){
		return size;
	}
	
	public long [] array(){
		return array;
	}
	

	public void clear() {
		this.count = 0;
	}
}
