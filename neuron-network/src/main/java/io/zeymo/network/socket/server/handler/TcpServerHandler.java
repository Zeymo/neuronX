package io.zeymo.network.socket.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.socket.rpc.RpcServerComponent;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

public class TcpServerHandler extends ChannelInboundHandlerAdapter {

	private Logger						log	= NetworkLogUtils.getLogger();
	private final RpcServerComponent[]	components;

	public TcpServerHandler(RpcServerComponent.Factory[] factories) {
		this.components = new RpcServerComponent[factories.length];
		for (int i = 0; i < factories.length; ++i) {
			if (factories[i] != null) {
				this.components[i] = factories[i].newInstance();
			}
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
		if (message instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) message;
			int protocol = buf.readByte();
			if (protocol == Protocol.RPC) {
				int componentId = buf.readByte();
				int sessionId = buf.readByte();
				long sessionTick = buf.readLong();
				int index = buf.readByte();

				components[componentId].writeRpcResponse(sessionId, sessionTick, index, buf, context);
				ReferenceCountUtil.release(buf);
			} else {
				buf.resetReaderIndex();
				super.channelRead(context, message);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable exception) throws Exception {
		log.error("[TcpServerHandler::exceptionCaught]Exception..." + exception, exception);
	}

	@Override
	public void channelWritabilityChanged(ChannelHandlerContext context) throws Exception {
		if (!context.channel().isWritable()) {
			log.error("[TcpServerHandler::channelWritabilityChanged]" + ChannelGroupContext.hostNameOf(context.channel()) + " is too busy");
		}
		super.channelWritabilityChanged(context);
	}
}
