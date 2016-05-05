package io.zeymo.network.socket.client.handler.spi;

import io.netty.buffer.ByteBuf;
import io.zeymo.network.annotation.External;
import io.zeymo.network.context.RemoteContextRingBuffer;
import io.zeymo.network.socket.rpc.RpcClientComponent;

/**
 * Created By Zeymo at 14/12/2 15:22
 */
@External
public class DefaultRpcClientComponent implements RpcClientComponent {

	private final RemoteContextRingBuffer buffer;

	public DefaultRpcClientComponent(RemoteContextRingBuffer buffer) {
		this.buffer = buffer;
	}

	public void receiveRpcResponse(int sessionId, long sessionTick, int index, ByteBuf buf) {
		this.buffer.complete(sessionId, sessionTick, index, buf);
	}
}
