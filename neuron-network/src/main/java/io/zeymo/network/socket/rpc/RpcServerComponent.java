package io.zeymo.network.socket.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created By Zeymo at 14/12/16 14:40
 */
public interface RpcServerComponent {

	public static interface Factory {

		public int getComponentId();

		public RpcServerComponent newInstance();
	}

	public abstract void writeRpcResponse(int sessionId, long sessionTick, int index, ByteBuf buf, ChannelHandlerContext context);

}
