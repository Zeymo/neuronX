package io.zeymo.network.socket.rpc;

import io.netty.buffer.ByteBuf;

/**
 * Created By Zeymo at 14/12/16 14:40
 */
public interface RpcClientComponent {

	public void receiveRpcResponse(int sessionId, long sessionTick, int index, ByteBuf buf);
}
