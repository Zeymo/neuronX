package io.zeymo.network.customize.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.util.ChannelGroupLogUtils;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

/**
 * Created by salah.liuyj on 2015/4/17.
 */
public class CustomizeHandShakeHandler extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private ChannelGroupContext channels;

    public CustomizeHandShakeHandler(ChannelGroupContext channels) {
        this.channels = channels;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause)
            throws Exception {
        log.error("[Client-HeartBeat-Handler]Exception...",cause);
    }

    @Override
    public void channelActive(ChannelHandlerContext context) throws Exception {
        int bodyLength = Protocol.SIZE_OF_BYTE + Protocol.SIZE_OF_INT;
        int frameLength = bodyLength + Protocol.SIZE_OF_INT;
        ByteBuf ping = context.channel().alloc().buffer(frameLength);
        ping.writeInt(bodyLength);
        ping.writeByte(Protocol.HAND_SHAKE);
        ping.writeInt(-999);
        context.writeAndFlush(ping);
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        channels.remove(context.channel());
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) message;
            int protocol = buf.readByte();
            if(protocol == Protocol.HAND_SHAKE){
                int machineId = buf.readInt();
                if(machineId >= 0){
                    channels.add(machineId, context.channel());
                    ChannelGroupLogUtils.onCreate(log, context.channel(), channels);
                }
                else {
                    log.error("[Client-HeartBeat-Handler] matrix node " + ChannelGroupContext.hostNameOf(context.channel()) + " data is broken");
                }
                ReferenceCountUtil.release(message);

            }else{
                buf.resetReaderIndex();
                super.channelRead(context, buf);
            }
        }
    }

}
