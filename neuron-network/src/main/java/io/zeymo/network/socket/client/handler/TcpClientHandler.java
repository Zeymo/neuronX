package io.zeymo.network.socket.client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.socket.client.handler.spi.DefaultRpcClientComponent;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

public class TcpClientHandler extends ChannelInboundHandlerAdapter {

	private Logger			log	= NetworkLogUtils.getLogger();
	private DefaultRpcClientComponent component;

	public TcpClientHandler(DefaultRpcClientComponent component) {
		this.component = component;
	}

	@Override
	public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
		if (message instanceof ByteBuf) {
			ByteBuf buf = (ByteBuf) message;
			int protocol = buf.readByte();
			if (protocol == Protocol.RPC) {
				buf.skipBytes(1);
				int sessionId = buf.readByte();
				long sessionTick = buf.readLong();
				int index = buf.readByte();

				long tick = System.currentTimeMillis();
				if (sessionTick > tick) {
					if (NetworkLogUtils.DEBUG) {
						log.debug("RPC response >>> " + sessionId + ", tick : " + tick + ", len : " + buf.readableBytes());
					}
					component.receiveRpcResponse(sessionId, sessionTick, index, buf);
				} else {
					if (NetworkLogUtils.DEBUG) {
						log.debug("dropped response for expired session id : " + sessionId + ", tick : " + sessionTick);
					}
				}
				ReferenceCountUtil.release(buf);
			} else {
				buf.resetReaderIndex();
				super.channelRead(context, message);
			}
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
		log.error("[TcpClientHandler::exceptionCaught]Exception..." + cause);
		super.exceptionCaught(context, cause);
	}
}
