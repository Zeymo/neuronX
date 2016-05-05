package io.zeymo.network.socket.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.zeymo.network.annotation.Internal;
import io.zeymo.network.context.RemoteContextRingBuffer;
import io.zeymo.network.schema.ClientLayout;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.schema.NetworkLayout;
import io.zeymo.network.schema.ProtocolLayout;
import io.zeymo.network.socket.PluginInitializer;
import io.zeymo.network.socket.client.handler.HandShakeHandler;
import io.zeymo.network.socket.client.handler.HeartBeatHandler;
import io.zeymo.network.socket.client.handler.TcpClientHandler;
import io.zeymo.network.socket.client.handler.spi.DefaultRpcClientComponent;
import io.zeymo.network.socket.rpc.Handler;
import io.zeymo.network.socket.rpc.RPC;
import io.zeymo.network.util.NamedThreadFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created By Zeymo at 14-9-25 17:51
 */
@Internal
public class NettyClient extends RPC {
	protected final Bootstrap							bootstrap;
	protected final int									port;
	protected final int									connectTimeout;
	protected final int									delay;
	protected final EventLoopGroup						workerGroup;
	protected volatile ScheduledFuture<?>				reconnectExecutorFuture;
	private final MatrixLayout matrix;
	protected static final ScheduledThreadPoolExecutor	reconnectExecutorService	= new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("ClientReconnectTimer", true));
	protected final ConcurrentHashMap<String, Object>	connectionLocks;

	public NettyClient(final MatrixLayout matrix, final NetworkLayout layout) {
		super("TBP-CLIENT-CHANNEL-GROUP", layout.getRemoteContextSize());
		this.matrix = matrix;
		this.connectionLocks = new ConcurrentHashMap<String, Object>();

		final ClientLayout clientLayout = layout.getClientLayout();
		final ProtocolLayout protocolLayout = layout.getProtocolLayout();
		port = layout.getPort();
		connectTimeout = clientLayout.getConnectTimeout();
		delay = clientLayout.getReconnectDelay();

		workerGroup = new NioEventLoopGroup(clientLayout.getWorkThreadNum(), new NamedThreadFactory("NettyClient"));
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup).channel(NioSocketChannel.class);

		final RemoteContextRingBuffer remoteContext = super.remoteContext;

		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(Handler.INITIALIZE.name, new PluginInitializer(plugins));
				pipeline.addLast(Handler.TIMEOUT.name, new IdleStateHandler(protocolLayout.getPingTimeout(), 0, 0, TimeUnit.MILLISECONDS));
				pipeline.addLast(Handler.DECODER.name,
						new LengthFieldBasedFrameDecoder(protocolLayout.getMaxFrameLength(), protocolLayout.getLengthFieldOffset(), protocolLayout.getLengthFieldLength(), protocolLayout.getLengthAdjustment(), protocolLayout.getInitialBytesToStrip()));
				pipeline.addLast(Handler.HANDSHAKE.name, new HandShakeHandler(matrix, channels));
				pipeline.addLast(Handler.HEARTBEAT.name, new HeartBeatHandler(matrix, channels));
				pipeline.addLast(Handler.RPC.name, new TcpClientHandler(new DefaultRpcClientComponent(remoteContext)));
			}
		});
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
	}

	public void start() {
		try {
			// connect();
			timer();
		} catch (Exception e) {
			log.error("[NettyClient::start]Exception...", e);
		} finally {

		}
	}

	protected void connect() {
		MatrixLayout.MatrixNode localNode = matrix.getLocalNode();
		for (MatrixLayout.MatrixNode node : matrix.getNodeList()) {

			if (node.getId() != localNode.getId() && !channels.contains(node.getId())) {
				start0(node);
			}
		}
	}

	protected void start0(MatrixLayout.MatrixNode node) {
		String hostName = node.getHostName();
		ChannelFuture future = bootstrap.connect(hostName, port);

		if (this.connectionLocks.putIfAbsent(node.getConnectionName(), this) == null) {
			try {
				boolean isSuccess = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
				if (!isSuccess) {
					if (future.cause() != null) {
						log.warn("[NettyClient::start0]Exception..." + "client failed to connect to server " + hostName + ", error message:" + future.cause().getMessage());
					} else {
						log.warn("[NettyClient::start0]Exception..." + "client failed to connect to server " + hostName);
					}
				}
			} finally {
				this.connectionLocks.remove(node.getConnectionName());
			}
		}
	}

	protected void timer() {
		log.info("[NettyClient::timer]start...");
		if (reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled()) {
			Runnable connectStatusCheckCommand = new Runnable() {
				public void run() {
					connect();
				}
			};
			reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, 0, delay, TimeUnit.MILLISECONDS);
		}
	}

	public void shutdown() {
		workerGroup.shutdownGracefully();
		reconnectExecutorService.shutdown();
		channels.close().awaitUninterruptibly();
	}
}
