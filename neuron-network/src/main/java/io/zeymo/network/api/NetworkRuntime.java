package io.zeymo.network.api;

import io.zeymo.commons.utils.GsonUtils;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.domain.HostStatus;
import io.zeymo.network.message.Notification;
import io.zeymo.network.schema.MatrixLayout;
import io.zeymo.network.schema.NetworkLayout;
import io.zeymo.network.socket.client.NettyClient;
import io.zeymo.network.socket.server.NettyServer;

/**
 * Created By Zeymo at 15/1/8 17:18
 */
public class NetworkRuntime {

	public static final boolean				DEBUG	= true;
	private final NettyClient client;
	private final NettyServer server;
	private final Notification.Publisher	publisher;

	public NetworkRuntime(MatrixLayout matrix, NetworkLayout layout) {
		this.client = new NettyClient(matrix, layout);
		this.server = new NettyServer(matrix, layout);
		this.publisher = Notification.asPushlisher(layout.getNotificationLayout());
	}

	public void publishServerStatus(HostStatus hostStatus) {
		publisher.publish(GsonUtils.toPrettyString(hostStatus));
	}

	public void shutdown() {
		this.client.shutdown();
		this.server.shutdown();
	}

	public NettyClient getClient() {
		return client;
	}

	public NettyServer getServer() {
		return server;
	}

	public ChannelGroupContext getClientChannels() {
		return client.getChannels();
	}

	public ChannelGroupContext getServerChannels() {
		return server.getChannels();
	}

}
