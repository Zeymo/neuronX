package io.zeymo.network.api;

import io.netty.channel.ChannelHandlerContext;

public class ResponseParam {
	private byte[]					buffer;
	private int						bufferLength;

	private final int				componentId;
	private ChannelHandlerContext	context;
	private long					sessionTick;

	private int						index;
	private int						sessionId;

	public ResponseParam(int componentId) {
		this.componentId = componentId;
	}

	public byte[] getBuffer() {
		return buffer;
	}

	public int getBufferLength() {
		return bufferLength;
	}

	public int getComponentId() {
		return componentId;
	}

	public long getSessionTick() {
		return sessionTick;
	}

	public ChannelHandlerContext getContext() {
		return context;
	}

	public int getIndex() {
		return index;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setResponse(byte[] buffer, int length) {
		this.buffer = buffer;
		this.bufferLength = length;
	}

	public void set(int sessionId, long sessionTick, int index, ChannelHandlerContext context) {
		this.sessionId = sessionId;
		this.sessionTick = sessionTick;
		this.index = index;
		this.context = context;
	}

}
