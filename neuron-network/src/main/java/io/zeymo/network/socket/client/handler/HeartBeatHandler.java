package io.zeymo.network.socket.client.handler;

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

public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private MatrixLayout matrix;

    private ChannelGroupContext channels;

    public HeartBeatHandler(ChannelGroupContext channels) {
        new HeartBeatHandler(null, channels);
    }

    public HeartBeatHandler(MatrixLayout matrix,ChannelGroupContext channels) {
        this.matrix = matrix;
        this.channels = channels;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                log.info("[Client-HeartBeat-Handler]idle event triggered,can't touch "+context.channel());
                context.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
            throws Exception {
        log.error("[Client-HeartBeat-Handler]Exception...",cause);
    }

/*    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        if(matrix != null){
            context.channel().metadata()
            Channel channel = context.channel();
            MatrixLayout.MatrixNode node = matrix.getNode(ChannelGroupContext.hostNameOf(channel));
            if(node == null) log.error("[Client-HeartBeat-Handler] matrix node " + ChannelGroupContext.hostNameOf(channel) + " data is broken");
            channels.add(node.getId(),channel);
            ChannelGroupLogUtils.onCreate(log, context.channel(),channels);
        }
    }*/

/*    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        //channels.remove(context.channel()); instead by ChannelGroupContext::remover0
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
    }*/

    @Override
	public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof ByteBuf){
            ByteBuf ping = (ByteBuf) message;
            byte protocol = ping.readByte();
            if(protocol == Protocol.HEART_BEAT){
                ReferenceCountUtil.release(message);
                int bodyLength = Protocol.SIZE_OF_BYTE;
                int frameLength = bodyLength + Protocol.SIZE_OF_INT;
                ByteBuf pong = context.channel().alloc().buffer(frameLength);
                pong.writeInt(bodyLength);
                pong.writeByte(protocol);
                context.writeAndFlush(pong);
            }else{
                ping.resetReaderIndex();
                super.channelRead(context,ping);
            }
        }
	}

}
