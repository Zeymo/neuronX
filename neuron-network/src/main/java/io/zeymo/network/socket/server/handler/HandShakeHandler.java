package io.zeymo.network.socket.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.util.ChannelGroupLogUtils;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

/**
 * Created by salah.liuyj on 2015/4/17.
 */
public class HandShakeHandler extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private final MatrixLayout matrix;

    private final ChannelGroupContext channels;

    public HandShakeHandler(MatrixLayout matrix, ChannelGroupContext channels) {
        this.channels = channels;
        this.matrix = matrix;
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
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
    }*/

    @Override
    public void channelUnregistered(ChannelHandlerContext context) throws Exception {
        channels.remove(context.channel());
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            int protocol = buf.readByte();
            if(protocol == Protocol.HAND_SHAKE){
                int machineId = buf.readInt();
                log.error("####################server channelread handshake,machineid:"+machineId);
                if(machineId >= 0){
                    channels.add(machineId, context.channel());
                    ChannelGroupLogUtils.onCreate(log, context.channel(), channels);

                }
                else {
                    channels.addClient(context.channel());
                }
                int bodyLength = Protocol.SIZE_OF_BYTE + Protocol.SIZE_OF_INT;
                int frameLength = bodyLength + Protocol.SIZE_OF_INT;
                ByteBuf ping = context.channel().alloc().buffer(frameLength);
                ping.writeInt(bodyLength);
                ping.writeByte(Protocol.HAND_SHAKE);
                ping.writeInt(matrix.getLocalNode().getId());
                context.writeAndFlush(ping);
                ReferenceCountUtil.release(msg);

            }else{
                buf.resetReaderIndex();
                super.channelRead(context, buf);
            }
        }
    }

}

