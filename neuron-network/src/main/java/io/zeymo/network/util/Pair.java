package io.zeymo.network.util;

/**
 * Created By Zeymo at 14-10-10 11:05
 */
public class Pair<F, S> {

	private F	first;

	private S	second;

	private Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public static <F, S> Pair<F, S> of(F first, S second) {
		return new Pair<F, S>(first, second);
	}

	public F getFirst() {
		return first;
	}

	public S getSecond() {
		return second;
	}
}
