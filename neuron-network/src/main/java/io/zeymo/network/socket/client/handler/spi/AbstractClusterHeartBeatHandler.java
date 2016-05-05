package io.zeymo.network.socket.client.handler.spi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
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

public abstract class AbstractClusterHeartBeatHandler extends ChannelInboundHandlerAdapter {

    protected Logger log = NetworkLogUtils.getLogger();

    private final MatrixLayout matrix;

    private final ChannelGroupContext channels;

    public AbstractClusterHeartBeatHandler(MatrixLayout matrix, ChannelGroupContext channels) {
        this.matrix = matrix;
        this.channels = channels;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state().equals(IdleState.READER_IDLE)){
                log.info("[Client-Cluster-BeatHandler]idle event triggered,can't touch "+context.channel());
                context.channel().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
            throws Exception {
        log.error("[Client-Cluster-Handler]Exception...",cause);
    }

/*    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        onActive(id);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        onInactive(id);
    }*/

    @Override
	public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof ByteBuf){
            ByteBuf ping = (ByteBuf) message;
            byte protocol = ping.readByte();
            if(protocol == Protocol.HEART_BEAT){
                doClusterHandler(ping, context.channel());
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

    protected abstract void doClusterHandler(ByteBuf buf,Channel channel);



}
