package io.zeymo.neuron;

import io.zeymo.commons.io.BinaryWritable;
import io.zeymo.commons.io.NeuronReader;
import io.zeymo.commons.io.NeuronWriter;
import io.zeymo.neuron.domain.Varchar;

import java.io.IOException;

public class NeuronRequest implements BinaryWritable {
	public static void protocolWrite(NeuronWriter writer, NeuronRequest requestTemplate, int nodeCount, long[] nodes) throws IOException {
		requestTemplate.taskName.write(writer);
		writer.writeUVInt((int) requestTemplate.timeoutMs);
		writer.writeBoolean(requestTemplate.allowPartialResponse);
		writer.writeBoolean(requestTemplate.initialInvoke);
		writer.writeBoolean(requestTemplate.quickResponse);
		// writer.writeUVInt(requestTemplate.type.code);

		writer.writeUVInt(nodeCount);
		for (int i = 0; i < nodeCount; ++i) {
			writer.writeFLong(nodes[i]);
		}

		writer.writeUVInt(requestTemplate.paramLength);
		writer.writeBytes(requestTemplate.paramBuffer, 0, requestTemplate.paramLength);

	}

	/**
	 * 允许请求在返回值不完整的情况下不抛timeout异常，正常返回
	 */
	private boolean			allowPartialResponse;

	/**
	 * 用于保护集群不再乱派发请求，防止广播风暴<br>
	 * 原则很简单<br>
	 * <ul>
	 * <li>所有引擎端收到的request在被 "落盘"、"执行"、"转发"(通过RPC请求到远程，不是制定dispatchPlan阶段)之前，设置为false</li>
	 * <li>当且仅当所有客户端提交的请求，initialInvoke参数全部为true</li>
	 * </ul>
	 */
	private boolean			initialInvoke;
	private int				nodeCount;
	private long			nodes[];
	private final byte[]	paramBuffer;
	private int				paramLength;
	/**
	 * 对于UPDATE类型的请求，或者犯贱只是压测一下MAP/REDUCE性能，可以在Request中标明quickResponse=true<br>
	 * 引擎会在成功写入WAL之后直接通过RPC返回success <br>
	 * 【注意】task的具体执行过程中仍然有可能出错
	 */
	private boolean			quickResponse;
	private final Varchar taskName;
	private long			timeoutMs;

	public NeuronRequest() {
		this(new byte[NeuronConstants.RUNTIME_RPC_MAX_REQUEST_SIZE]);
	}

	public NeuronRequest(byte[] buffer) {
		this.initialInvoke = true;
		this.taskName = new Varchar(new byte[NeuronConstants.RUNTIME_MAX_TASK_NAME_LENGTH]);
		this.nodes = new long[NeuronConstants.RUNTIME_MAX_NODE_QUERY_BATCH];
		this.paramBuffer = buffer;
	}

	public NeuronRequest(String taskName, int bufferSize) {
		this(new Varchar(taskName), new byte[bufferSize]);
	}

	public NeuronRequest(Varchar sharedTaskName, byte[] buffer) {
		this.initialInvoke = true;
		this.allowPartialResponse = false;
		this.quickResponse = false;
		this.taskName = sharedTaskName;
		this.nodes = new long[NeuronConstants.RUNTIME_MAX_NODE_QUERY_BATCH];
		this.paramBuffer = buffer;
	}

	public void addNode(long node) {
		int index = this.nodeCount + 1;
		if (index == NeuronConstants.RUNTIME_MAX_NODE_QUERY_BATCH) {
			throw new IndexOutOfBoundsException("too many nodes in a batch query.");
		}
		nodes[this.nodeCount++] = node;

	}

	private void clearNodes() {
		this.nodeCount = 0;
	}

	public String debug() {
		StringBuilder sb = new StringBuilder();
		sb.append("NeuronRequest[");
		sb.append(taskName);
		sb.append(":[");
		if (nodeCount > 0) {
			sb.append(nodes[0]);
		}
		for (int i = 1; i < nodeCount; ++i) {
			sb.append(',');
			sb.append(nodes[i]);

		}
		sb.append("], initial:");
		sb.append(initialInvoke);
		sb.append(", partial:");
		sb.append(allowPartialResponse);
		sb.append(", quick:");
		sb.append(quickResponse);
		sb.append(", timeout:");
		sb.append(timeoutMs);
		sb.append(", len:");
		sb.append(paramLength);
		sb.append("]");
		return sb.toString();
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public long[] getNodes() {
		return nodes;
	}

	public byte[] getParamBuffer() {
		return paramBuffer;
	}

	public int getParamLength() {
		return paramLength;
	}

	public Varchar getTaskName() {
		return taskName;
	}

	public long getTimeoutMs() {
		return timeoutMs;
	}

	public boolean isAllowPartialResponse() {
		return allowPartialResponse;
	}

	public boolean isInitialInvoke() {
		return initialInvoke;
	}

	public boolean isQuickResponse() {
		return quickResponse;
	}

	@Override
	public void readFields(NeuronReader reader) throws IOException {

		this.taskName.readFields(reader);
		this.timeoutMs = reader.readUVInt();
		this.allowPartialResponse = reader.readBoolean();
		this.initialInvoke = reader.readBoolean();
		this.quickResponse = reader.readBoolean();
		this.nodeCount = reader.readUVInt();
		for (int i = 0; i < this.nodeCount; ++i) {
			this.nodes[i] = reader.readFLong();
		}

		paramLength = reader.readUVInt();
		reader.readBytes(paramBuffer, 0, paramLength);
	}

	public void reset(boolean allowPartialResponse, boolean initialInvoke, boolean quickResponse, int requestSize, long timeoutMs) {
		this.allowPartialResponse = allowPartialResponse;
		this.initialInvoke = initialInvoke;
		this.quickResponse = quickResponse;
		this.clearNodes();
		this.timeoutMs = timeoutMs;
		this.paramLength = requestSize;
	}

	public void setAllowPartialResponse(boolean allowPartialResponse) {
		this.allowPartialResponse = allowPartialResponse;
	}

	public void setInitialInvoke(boolean initialInvoke) {
		this.initialInvoke = initialInvoke;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}

	public void setNodes(long[] nodes) {
		this.nodes = nodes;
	}

	public void setParamLength(int paramLength) {
		this.paramLength = paramLength;
	}

	public void setQuickResponse(boolean quickResponse) {
		this.quickResponse = quickResponse;
	}

	public void setTaskName(String taskName) {
		this.taskName.set(taskName);
	}

	public void setTimeoutMs(long timeoutMs) {
		this.timeoutMs = timeoutMs;
	}

	@Override
	public void write(NeuronWriter writer) throws IOException {
		protocolWrite(writer, this, nodeCount, nodes);
	}

}
