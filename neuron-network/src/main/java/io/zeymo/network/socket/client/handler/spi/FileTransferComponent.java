package io.zeymo.network.socket.client.handler.spi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Created By Zeymo at 14/12/2 10:04
 */
public interface FileTransferComponent {

	void receiveFileMeta0(int sessionId, int index, ByteBuf buf);

	void receiveChunkedFile0(int sessionId, int index, ByteBuf buf);

	void writeFileMeta(int sessionId, int index, ByteBuf buf, ChannelHandlerContext context);

	void receiveChunkedFile(int sessionId, int index, ByteBuf buf);

}
