package io.zeymo.network.socket.server.handler.spi;

import io.zeymo.network.annotation.External;
import io.zeymo.network.context.RemoteContextRingBuffer;
import io.zeymo.network.socket.client.handler.spi.DefaultRpcClientComponent;
import io.zeymo.network.socket.rpc.RpcServerComponent;

/**
 * Created By Zeymo at 14/12/2 15:22
 */
@External
public abstract class DuplexRpcComponent extends DefaultRpcClientComponent implements RpcServerComponent {

	public DuplexRpcComponent(RemoteContextRingBuffer buffer) {
		super(buffer);
	}

}
