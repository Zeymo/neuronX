package io.zeymo.network.api;

import java.nio.ByteBuffer;

public class DefaultRequestParam implements RequestParam {
	private final int			componentId;
	private int					machineId;
	private final ByteBuffer	request;
	private final ByteBuffer	response;

	public DefaultRequestParam(int componentId, ByteBuffer request, ByteBuffer response) {
		this.componentId = componentId;
		this.request = request;
		this.response = response;
	}

	@Override
	public void complete(int sessionId, byte[] responseBuffer, int offset, int length) {
		this.response.clear();
		this.response.put(responseBuffer, offset, length);
		this.response.flip();
	}

	@Override
	public int getComponentId() {
		return componentId;
	}

	@Override
	public int getMachineId() {
		return machineId;
	}

	public ByteBuffer getRequest() {
		return request;
	}

	@Override
	public byte[] getRequestBuffer() {
		return request.array();
	}

	@Override
	public int getRequestLength() {
		return this.request.remaining();
	}

	public ByteBuffer getResponse() {
		return response;
	}

	@Override
	public byte[] getResponseBuffer() {
		return this.response.array();
	}

	@Override
	public int getResponseLength() {
		return this.response.remaining();
	}

	public void setMachineId(int machineId) {
		this.machineId = machineId;
	}

}
