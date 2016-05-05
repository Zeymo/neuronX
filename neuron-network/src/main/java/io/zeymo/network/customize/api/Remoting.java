package io.zeymo.network.customize.api;

import io.zeymo.network.annotation.External;
import io.zeymo.network.api.RequestParam;
import io.zeymo.network.customize.socket.NetworkClient;
import io.zeymo.network.socket.rpc.Protocol;

import java.io.IOException;

/**
 * Created By Zeymo at 14-9-26 10:24
 */
@External
public class Remoting {

	private final NetworkClient socket;

	public Remoting(NetworkClient socket) throws IOException {
		this.socket = socket;
	}

	public int balanceMachineId() {
		return socket.channelBalancer();
	}

	public void invoke(RequestParam param, long timeout) throws IOException {
		socket.invokeOnce(Protocol.RPC, param, timeout);
	}
}
