package io.zeymo.neuron.runtime;

import io.zeymo.commons.io.impl.NeuronByteArrayWriter;
import io.zeymo.neuron.assembly.domain.XNode;
import io.zeymo.neuron.io.BufferParam;

import java.io.IOException;

public class NeuronAssemblyRuntime {
	private final XNode node;
	private final NeuronCodecRuntime	codecRuntime;
	private final NeuronByteArrayWriter writer;

	public int build(BufferParam param) throws IOException {
		writer.reset(param.getBuffer());
		node.build(writer);
		int length = writer.getPosition();
		param.setLength(length);
		return length;
	}

	// public XNode getNode() {
	// return node;
	// }

	public NeuronCodecRuntime getCodecRuntime() {
		return codecRuntime;
	}

	public NeuronAssemblyRuntime(NeuronConfiguration configuration, NeuronCodecRuntime codecRuntime) {
		this.codecRuntime = codecRuntime;
		this.node = new XNode(configuration);
		this.writer = new NeuronByteArrayWriter();
	}

	public XNode resetNode(long nodeId) {
		node.reset(nodeId);
		return node;
	}

	public XNode loadNode(BufferParam param) throws IOException {
		this.codecRuntime.loadNode(param);
		this.node.reset(codecRuntime);
		return this.node;
	}
}
