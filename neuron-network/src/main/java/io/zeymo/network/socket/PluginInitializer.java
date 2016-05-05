package io.zeymo.network.socket;

import com.google.common.base.Supplier;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.zeymo.network.socket.rpc.Handler;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

import java.util.Map;

/**
 * Created By Zeymo at 15/1/13 09:11
 */
public class PluginInitializer extends ChannelInboundHandlerAdapter {

    private Logger log = NetworkLogUtils.getLogger();

    private final Map<Handler, Supplier<ChannelHandler>> plugins;

    public PluginInitializer(Map<Handler, Supplier<ChannelHandler>> plugins) {
        this.plugins = plugins;
    }

    @Override
    public final void channelRegistered(ChannelHandlerContext context) throws Exception {
        ChannelPipeline pipeline = context.pipeline();
        boolean success = false;
        try {
            attachPlugins(pipeline, plugins);
            pipeline.remove(this);
            context.fireChannelRegistered();
            success = true;
        } catch (Throwable t) {
            log.error("Failed to initialize a channel. Closing: " + context.channel(), t);
        } finally {
            if (pipeline.context(this) != null) {
                pipeline.remove(this);
            }
            if (!success) {
                context.close();
            }
        }
    }

    private void attachPlugins(ChannelPipeline pipeline,Map<Handler,Supplier<ChannelHandler>> plugins){
        if (plugins.size() != 0) {
            for (Handler handler : plugins.keySet()) {
                String name = handler.name;
                ChannelHandler exist = pipeline.get(name);
                ChannelHandler handler0 = plugins.get(handler).get();
                if (exist != null) {
                    pipeline.replace(name, name, handler0);
                } else {
                    pipeline.addLast(name, handler0);
                }
            }
        }
    }
}
