package io.zeymo.network.socket.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.zeymo.network.annotation.Internal;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.schema.NetworkLayout;
import io.zeymo.network.schema.ProtocolLayout;
import io.zeymo.network.schema.ServerLayout;
import io.zeymo.network.socket.PluginInitializer;
import io.zeymo.network.socket.rpc.Handler;
import io.zeymo.network.socket.rpc.RPC;
import io.zeymo.network.socket.rpc.RpcServerComponent;
import io.zeymo.network.socket.server.handler.HandShakeHandler;
import io.zeymo.network.socket.server.handler.HeartBeatHandler;
import io.zeymo.network.socket.server.handler.TcpServerHandler;
import io.zeymo.network.util.NamedThreadFactory;

import java.util.concurrent.TimeUnit;

@Internal
public class NettyServer extends RPC {

	private final ServerBootstrap				bootstrap;
	private final EventLoopGroup				bossGroup;
	private final EventLoopGroup				workerGroup;
	private final RpcServerComponent.Factory[]	componentFactories;
	private final int							port;

	public void registerServerComponent(RpcServerComponent.Factory factory) {
		if (componentFactories[factory.getComponentId()] != null)
			throw new RuntimeException("duplicate component id of " + factory.getComponentId());
		componentFactories[factory.getComponentId()] = factory;
	}

	public NettyServer(final MatrixLayout matrix, final NetworkLayout layout) {
		super("TBP-SERVER-CHANNEL-GROUP", layout.getRemoteContextSize());
		final ServerLayout serverLayout = layout.getServerLayout();
		final ProtocolLayout protocolLayout = layout.getProtocolLayout();

		this.port = layout.getPort();
		bossGroup = new NioEventLoopGroup(serverLayout.getBossThreadNum(), new NamedThreadFactory("NettyReactor"));
		workerGroup = new NioEventLoopGroup(serverLayout.getWorkThreadNum(), new NamedThreadFactory("NettyServer"));
		componentFactories = new RpcServerComponent.Factory[Byte.MAX_VALUE];
		bootstrap = new ServerBootstrap();

		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(Handler.INITIALIZE.name, new PluginInitializer(plugins));
				pipeline.addLast(Handler.TIMEOUT.name, new IdleStateHandler(protocolLayout.getPingTimeout(), 0, protocolLayout.getPingScheduleTime(), TimeUnit.MILLISECONDS));
				pipeline.addLast(Handler.DECODER.name,
						new LengthFieldBasedFrameDecoder(protocolLayout.getMaxFrameLength(), protocolLayout.getLengthFieldOffset(), protocolLayout.getLengthFieldLength(), protocolLayout.getLengthAdjustment(), protocolLayout.getInitialBytesToStrip()));
				pipeline.addLast(Handler.HANDSHAKE.name, new HandShakeHandler(matrix,channels));
				pipeline.addLast(Handler.HEARTBEAT.name, new HeartBeatHandler(matrix, channels));
				pipeline.addLast(Handler.RPC.name, new TcpServerHandler(componentFactories));
			}
		});
		// bootstrap.option(ChannelOption.TCP_NODELAY, true);
		// bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
	}

	public void start() {
		try {
			ChannelFuture future = bootstrap.bind(port).sync();
			if (future.isSuccess()) {
				log.debug("[NettyServer::start]server start .......");
			}
		} catch (Exception e) {
			log.debug("[NettyServer::start]server start exception...", e);
			shutdown();
		}
	}

	public void shutdown() {
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		channels.close().awaitUninterruptibly();
	}

}
