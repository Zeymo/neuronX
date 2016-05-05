package io.zeymo.cache.memory;

/**
 * Created By Zeymo at 14-8-7 06:50
 */
public abstract class ArrangedContainer implements Container {

	private int	uniqueId;

	public ArrangedContainer() {
	}

	public ArrangedContainer(final int uniqueId) {
		this.uniqueId = uniqueId;
	}

	public int getUniqueId() {
		return this.uniqueId;
	}
}
