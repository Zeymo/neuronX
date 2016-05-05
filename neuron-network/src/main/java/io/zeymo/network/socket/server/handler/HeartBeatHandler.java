package io.zeymo.network.socket.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

/**
 * Created By Zeymo at 14-10-11 15:26
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private final MatrixLayout matrix;

    private final ChannelGroupContext channels;

    public HeartBeatHandler(MatrixLayout matrix,ChannelGroupContext channels) {
        this.matrix = matrix;
        this.channels = channels;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                log.info("[Server-HeartBeatHandler]idle event triggered,can't touch " + context.channel());
                context.channel().close();
            }else if(event.state().equals(IdleState.ALL_IDLE)){
                int bodyLength = Protocol.SIZE_OF_BYTE;
                int frameLength = bodyLength + Protocol.SIZE_OF_INT;
                ByteBuf ping = context.channel().alloc().buffer(frameLength);
                ping.writeInt(bodyLength);
                ping.writeByte(Protocol.HEART_BEAT);
                context.writeAndFlush(ping);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
            throws Exception {
        log.error("[Server-HeartBeat-Handler]Exception...",cause);
    }

/*    @Override
    public void channelRegistered(final ChannelHandlerContext context) throws Exception {
        String hostName = ChannelGroupContext.hostNameOf(context.channel());
        MatrixLayout.MatrixNode node = matrix.getNode(hostName);
        if(node == null){
            channels.addClient(context.channel());
            return;
        }
        int uid = node.getId();
        channels.add(uid, context.channel());
        ChannelGroupLogUtils.onRemove(log,context.channel(),channels);
    }*/

/*    @Override
    public void channelUnregistered(ChannelHandlerContext context) throws Exception {
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
    }*/

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            int protocol = buf.readByte();
            if(protocol == Protocol.HEART_BEAT){
                ReferenceCountUtil.release(msg);
            }else{
                buf.resetReaderIndex();
                super.channelRead(context, buf);
            }
        }
    }

}
