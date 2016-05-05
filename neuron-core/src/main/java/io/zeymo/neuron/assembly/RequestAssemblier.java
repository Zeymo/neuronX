package io.zeymo.neuron.assembly;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronOutputBuffer;
import io.zeymo.commons.io.impl.NeuronByteBufferWriter;
import io.zeymo.neuron.NeuronConstants;
import io.zeymo.neuron.NeuronRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

public class RequestAssemblier {
	private final byte[]					buffer;
	private final NeuronByteBufferWriter byteBufferWriter;
	private final NeuronOutputBuffer output;

	private final NeuronRequest request;

	public RequestAssemblier() {
		this.buffer = new byte[NeuronConstants.RUNTIME_RPC_MAX_REQUEST_SIZE];
		this.output = new NeuronOutputBuffer(buffer);
		this.request = new NeuronRequest(buffer);
		this.byteBufferWriter = new NeuronByteBufferWriter();
	}

	public NeuronRequest build() {
		return this.request;
	}

	public void build(ByteBuffer byteBuffer) throws IOException {
		byteBufferWriter.reset(byteBuffer);
		this.request.write(byteBufferWriter);
	}

	public void reset(String taskName, BinaryWritable param, boolean dispatch, long timeoutMs, long... nodes) throws IOException {
		request.setTaskName(taskName);
		request.setInitialInvoke(dispatch);
		request.setNodes(nodes);
		request.setNodeCount(nodes.length);
		request.setTimeoutMs(timeoutMs);

		output.reset(request.getParamBuffer());
		param.write(output);

		int paramLength = output.getPosition();
		request.setParamLength(paramLength);
	}
}
