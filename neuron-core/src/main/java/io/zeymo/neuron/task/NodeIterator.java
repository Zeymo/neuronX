package io.zeymo.neuron.task;

public class NodeIterator {
	private int		nodeCount;
	private int		nodeIndex;
	private long[]	nodes;

	public NodeIterator() {

	}

	public boolean hasNext() {
		return nodeIndex < nodeCount;
	}

	public void init(long[] nodes, int nodeCount) {
		this.nodeCount = nodeCount;
		this.nodes = nodes;
		this.nodeIndex = 0;
	}

	public long next() {
		if (!hasNext()) {
			return -1;
		}

		long nodeId = nodes[nodeIndex++];
		return nodeId;
	}
}
