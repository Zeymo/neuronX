package io.zeymo.network.socket.client.handler.spi;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.socket.client.handler.HeartBeatHandler;
import io.zeymo.network.socket.rpc.Protocol;
import io.zeymo.network.util.ChannelGroupLogUtils;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

/**
 * Created by salah.liuyj on 2015/4/20.
 */
public abstract class AbstractClusterHandShakeHandler extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private MatrixLayout matrix;

    private ChannelGroupContext channels;

    public AbstractClusterHandShakeHandler(ChannelGroupContext channels) {
        new HeartBeatHandler(null, channels);
    }

    public AbstractClusterHandShakeHandler(MatrixLayout matrix,ChannelGroupContext channels) {
        this.matrix = matrix;
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
        ping.writeInt(matrix.getLocalNode().getId());
        log.error("####################client active handshake");
        context.writeAndFlush(ping);

    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {

        int machineId = channels.getMachineId(context.channel());
        channels.remove(context.channel());
        ChannelGroupLogUtils.onRemove(log, context.channel(), channels);
        if(machineId >= 0){
            onInactive(machineId);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) message;
            int protocol = buf.readByte();
            if(protocol == Protocol.HAND_SHAKE){
                int machineId = buf.readInt();
                log.error("####################client channelread handshake,machineid:"+machineId);
                if(machineId >= 0){
                    channels.add(machineId, context.channel());
                    ChannelGroupLogUtils.onCreate(log, context.channel(), channels);
                    onActive(machineId);
                }
                else {
                    channels.addClient(context.channel());
                }
                ReferenceCountUtil.release(message);

            }else{
                buf.resetReaderIndex();
                super.channelRead(context, buf);
            }
        }
    }

    protected abstract void onActive(int id);

    protected abstract void onInactive(int id);

}
