package io.zeymo.neuron;

public enum NeuronRequestType {
	MAP_ONLY(0), MAP_REDUCE(1), UPDATE_ONLY(2);

	public final int	code;

	private NeuronRequestType(int code) {
		this.code = code;
	}

	public static NeuronRequestType parse(int code) {
		if (code == 0) {
			return MAP_ONLY;
		}
		if (code == 1) {
			return MAP_REDUCE;
		}
		if (code == 2) {
			return UPDATE_ONLY;
		}
		throw new IllegalArgumentException("illegal request type code : " + code);
	}
}
