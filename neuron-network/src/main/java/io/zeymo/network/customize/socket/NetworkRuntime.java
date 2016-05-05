package io.zeymo.network.customize.socket;

import io.zeymo.network.customize.api.Remoting;
import io.zeymo.network.schema.NetworkLayout;

import java.io.IOException;

/**
 * Created By Zeymo at 15/1/14 12:24
 */
public class NetworkRuntime {

	private final Remoting remoting;

	private final NetworkClient	client;

	public Remoting getRemoting() {
		return remoting;
	}

	public NetworkRuntime(NetworkLayout layout) throws IOException {
		client = new NetworkClient(layout);
		remoting = new Remoting(client);
	}

	public boolean checkPreload() {
		return client.checkPreload();
	}
}
