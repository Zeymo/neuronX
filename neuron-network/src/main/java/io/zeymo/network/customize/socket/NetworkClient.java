package io.zeymo.network.customize.socket;

import com.google.common.collect.Lists;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.zeymo.commons.utils.GsonUtils;
import io.zeymo.network.annotation.Internal;
import io.zeymo.network.context.RemoteContextRingBuffer;
import io.zeymo.network.customize.handler.CustomizeHandShakeHandler;
import io.zeymo.network.domain.HostStatus;
import io.zeymo.network.message.Notification;
import io.zeymo.network.schema.ClientLayout;
import io.zeymo.network.schema.NetworkLayout;
import io.zeymo.network.schema.ProtocolLayout;
import io.zeymo.network.socket.PluginInitializer;
import io.zeymo.network.socket.client.handler.HeartBeatHandler;
import io.zeymo.network.socket.client.handler.TcpClientHandler;
import io.zeymo.network.socket.client.handler.spi.DefaultRpcClientComponent;
import io.zeymo.network.socket.rpc.Handler;
import io.zeymo.network.socket.rpc.RPC;
import io.zeymo.network.util.ChannelGroupLogUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created By Zeymo at 14-9-25 17:51
 */
@Internal
public class NetworkClient extends RPC {

	/**
	 * 从全局配置(如configserver)推送过来的信息，重试链接次数<br>
	 * 这种情况发生在集群启动第一次收到通知时，或者有新的外部配置送达发生
	 */
	private final static int		GLOBAL_RETRY	= 30;

	/**
	 * 客户端与服务端发生链接断开，导致channelGroup.size==0时用于重新建立到服务器的链接<br>
	 * 由channelBalancer()方法触发reconnect()<br>
	 */
	private final static int		CACHED_RETRY	= 3;
	private final static int		seed			= 31415926;
	protected final Bootstrap		bootstrap;
	private final int				channelPoolSize;
	protected final int				connectTimeout;
	private final Logger			log				= LoggerFactory.getLogger(this.getClass());
	private List<String>			machineListCache;
	protected final int				port;

	/**
	 * 用于防止在同一时间的多个请求发出大量重连请求
	 */
	private AtomicInteger			reconnectCount;

	protected final EventLoopGroup	workerGroup;

	public NetworkClient(final NetworkLayout layout) {
		super("TBP-CLIENT-CHANNEL-GROUP", layout.getRemoteContextSize());
		this.channelPoolSize = layout.getChannelPoolSize();
		final ClientLayout clientLayout = layout.getClientLayout();
		final ProtocolLayout protocolLayout = layout.getProtocolLayout();
		port = layout.getPort();
		connectTimeout = clientLayout.getConnectTimeout();

		workerGroup = new NioEventLoopGroup(clientLayout.getWorkThreadNum());
		bootstrap = new Bootstrap();
		bootstrap.group(workerGroup).channel(NioSocketChannel.class);
		
		final RemoteContextRingBuffer remoteContext = super.remoteContext;

		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) throws Exception {
				ChannelPipeline pipeline = channel.pipeline();
				pipeline.addLast(Handler.INITIALIZE.name, new PluginInitializer(plugins));
				pipeline.addLast(Handler.TIMEOUT.name, new IdleStateHandler(protocolLayout.getPingTimeout(), 0, protocolLayout.getPingScheduleTime(), TimeUnit.MILLISECONDS));
				pipeline.addLast(Handler.DECODER.name,
						new LengthFieldBasedFrameDecoder(protocolLayout.getMaxFrameLength(), protocolLayout.getLengthFieldOffset(), protocolLayout.getLengthFieldLength(), protocolLayout.getLengthAdjustment(), protocolLayout.getInitialBytesToStrip()));
				pipeline.addLast(Handler.HANDSHAKE.name, new CustomizeHandShakeHandler(channels));
				pipeline.addLast(Handler.HEARTBEAT.name, new HeartBeatHandler(channels));
				pipeline.addLast(Handler.RPC.name, new TcpClientHandler(new DefaultRpcClientComponent(remoteContext)));
			}
		});
		bootstrap.option(ChannelOption.TCP_NODELAY, true);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.ALLOCATOR, new PooledByteBufAllocator());
		reconnectCount = new AtomicInteger(0);
		Notification.asSubscriber(layout.getNotificationLayout(), this);

	}

	public int channelBalancer() {
		int machineId = channels.balance();
		if (machineId == -1) {
			// 没有可供调用的服务器
			// 抛异常，顺便触发一次全局的reconnect
			this.triggerReconnect();
			throw new RuntimeException("no server availiable.");
		}
		return machineId;
	}

	public boolean checkPreload() {
		return channels.size() > 0;
	}

	private synchronized void reconnect(List<String> machines, int retryCount) {
		if (machines == null) {
			return;
		}

		List<HostStatus> aliveMachines = Lists.newArrayList();
		List<String> aliveMachineIps = Lists.newArrayList();
		for (String object : machines) {
			// AliveMachineDO machine = (AliveMachineDO) object;
			// AliveMachineDO machine = gson.fromJson((String) object, AliveMachineDO.class);
			HostStatus machine = GsonUtils.fromString(object, HostStatus.class);
			if (machine.isOnService()) {
				aliveMachines.add(machine);
				aliveMachineIps.add(machine.getHostName());
			}
		}

		Iterator<String> iterator = channels.clientKeySet().iterator();
		while (iterator.hasNext()) {
			String hostName = iterator.next();
			if (!aliveMachineIps.contains(hostName)) {
				Channel channel = channels.getClient(hostName);
				channels.remove(channel);
			}
		}

		int i = 0;
		int mod = aliveMachines.size();
		while (mod != 0 && channels.size() < channelPoolSize && i < retryCount) {
			HostStatus machine = aliveMachines.get((seed + i) % mod);
			if (!channels.contains(machine.getHostName())) {
				start(machine.getHostName(), machine.getId());
			}
			i++;
		}

		if (mod == 0) {
			log.warn("[NetworkClient::channels] there is no active channel now!");
		}

		if (channels.size() < channelPoolSize) {
			log.warn("[NetworkClient::channels] channel size is less then expect, current " + channels.size() + ",expect " + channelPoolSize);
		}
		this.machineListCache = machines;
	}

	public void handleData(List<String> machines) {
		this.reconnect(machines, GLOBAL_RETRY);
	}

	protected Channel start(String hostName, int machineId) {
		ChannelFuture future = bootstrap.connect(hostName, port);
		boolean isSuccess = future.awaitUninterruptibly(connectTimeout, TimeUnit.MILLISECONDS);
		if (isSuccess && future.isSuccess()) {
			Channel channel = future.channel();
			channels.add(machineId, channel);
			ChannelGroupLogUtils.onCreate(log, channel, channels);
		} else if (future.cause() != null) {
			log.warn("[NetworkClient::start0]Exception..." + "client failed to connect to server " + hostName + ", error message:" + future.cause().getMessage());
		} else {
			log.warn("[NetworkClient::start0]Exception..." + "client failed to connect to server " + hostName);
		}
		return future.channel();
	}

	private void triggerReconnect() {
		try {
			int count = reconnectCount.getAndIncrement();
			if (count == 0) {
				this.reconnect(machineListCache, CACHED_RETRY);
			}
		} finally {
			reconnectCount.decrementAndGet();
		}
	}
}
