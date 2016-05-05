package io.zeymo.network.socket.rpc;

import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.zeymo.network.api.RequestParam;
import io.zeymo.network.api.ResponseParam;
import io.zeymo.network.context.ChannelGroupContext;
import io.zeymo.network.context.RemoteContextRingBuffer;
import io.zeymo.network.util.NetworkLogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created By Zeymo at 14/11/21 16:57
 */
public abstract class RPC {

	public static final Charset	DEFAULT_CHARSET			= Charset.forName("UTF-8");
	public static final int		INTERNAL_COMPONENT_ID	= 0;
	public static final Logger	log						= NetworkLogUtils.getLogger();
	public static final int		ONCE_INVOKE_INDEX		= 0;

	private static void assembleRequest(int opCode, int sessionId, long sessionTick, int index, int componentId, byte[] requestBuffer, int requestLength, ByteBuf call) {
		call.writeInt(12 + requestLength);
		call.writeByte(opCode);
		call.writeByte(componentId);
		call.writeByte(sessionId);
		call.writeLong(sessionTick);
		call.writeByte(index);
		call.writeBytes(requestBuffer, 0, requestLength);
	}

	private static void assembleResponse(int opCode, int sessionId, long sessionTick, int index, int componentId, byte[] responseBuffer, int responseLength, ByteBuf call) {
		call.writeInt(12 + responseLength);
		call.writeByte(opCode);
		call.writeByte(componentId);
		call.writeByte(sessionId);
		call.writeLong(sessionTick);
		call.writeByte(index);
		call.writeBytes(responseBuffer, 0, responseLength);
	}

	public static void invokeResponse(int opCode, int sessionId, long sessionTick, int index, int componentId, byte[] responseBuffer, int responseLength, ChannelHandlerContext context) throws IOException {
		ByteBuf call = context.channel().alloc().buffer();
		assembleResponse(opCode, sessionId, sessionTick, index, componentId, responseBuffer, responseLength, call);
		context.writeAndFlush(call);
	}

	public static void invokeResponse(int opCode, int sessionId, long sessionTick, int index, RequestParam param, ChannelHandlerContext context) throws IOException {
		RPC.invokeResponse(opCode, sessionId, sessionTick, index, param.getComponentId(), param.getResponseBuffer(), param.getResponseLength(), context);
	}

	public static void invokeResponse(ResponseParam response) throws IOException {
		int sessionId = response.getSessionId();
		int index = response.getIndex();
		long sessionTick = response.getSessionTick();

		byte[] responseBuffer = response.getBuffer();
		int responseLength = response.getBufferLength();

		int componentId = response.getComponentId();
		ChannelHandlerContext context = response.getContext();

		RPC.invokeResponse(Protocol.RPC, sessionId, sessionTick, index, componentId, responseBuffer, responseLength, context);
	}

	protected final ChannelGroupContext channels;
	protected final Map<Handler, Supplier<ChannelHandler>> plugins	= Maps.newHashMap();
	protected final RemoteContextRingBuffer					remoteContext;

	protected RPC(String channelGroupName, int sessionCapacity) {
		this.channels = new ChannelGroupContext(channelGroupName, GlobalEventExecutor.INSTANCE);
		this.remoteContext = new RemoteContextRingBuffer(sessionCapacity);
	}

	public long getSessionTick(int sessionId) {
		return remoteContext.getTick(sessionId);
	}

	public int acquireSessionId(ArrayList<RequestParam> params, long timeout) {
		int sessionId = remoteContext.profile(params, timeout);
		return sessionId;
	}

	public int acquireSessionId(RequestParam param, long timeout) {
		int sessionId = remoteContext.profile(param, timeout);
		return sessionId;
	}

	public boolean await(int sessionId) throws InterruptedException {
		return remoteContext.await(sessionId);
	}

	public ChannelGroupContext getChannels() {
		return channels;
	}

	public void invokeAsync(int opCode, int sessionId, RequestParam param) throws IOException {
		long sessionTick = this.remoteContext.getTick(sessionId);
		invokeRequest(opCode, sessionId, sessionTick, ONCE_INVOKE_INDEX, param);
	}

	public boolean invokeBatch(int opCode, ArrayList<RequestParam> params, long timeout) throws IOException {
		int sessionId = this.acquireSessionId(params, timeout);
		long sessionTick = remoteContext.getTick(sessionId);

		try {
			this.invokeBatchAsync(opCode, sessionId, params);
			remoteContext.await(sessionId);
			return remoteContext.isDone(sessionId);
		} catch (InterruptedException e) {
			log.error("[RPC::invokeBatch]InterruptedException...", e);
		} finally {
			remoteContext.release(sessionId, sessionTick);
		}
		return false;
	}

	public void invokeBatchAsync(int opCode, int sessionId, ArrayList<RequestParam> params) throws IOException {
		// int sessionId = acquireSessionId(params);
		int batchSize = params.size();
		long sessionTick = remoteContext.getTick(sessionId);
		for (int index = 0; index < batchSize; ++index) {
			invokeRequest(opCode, sessionId, sessionTick, index, params.get(index));
		}
		// return sessionId;
	}

	public boolean invokeOnce(int opCode, RequestParam param, long timeout) throws IOException {
		int sessionId = remoteContext.profile(param, timeout);
		long sessionTick = remoteContext.getTick(sessionId);
		try {
			invokeRequest(opCode, sessionId, sessionTick, ONCE_INVOKE_INDEX, param);
			this.await(sessionId);
			return remoteContext.isDone(sessionId);
		} catch (InterruptedException e) {
			log.error("[RPC::invokeOnce]InterruptedException...", e);
		} catch (IOException e) {
			log.error("[RPC::invokeOnce]IOException...", e);
		} finally {
			this.release(sessionId, sessionTick);
		}
		return false;
	}

	protected void invokeRequest(int opCode, int sessionId, long sessionTick, int index, RequestParam param) throws IOException {
		int remoteId = param.getMachineId();
		Channel channel = channels.getServer(remoteId);
		if (channel == null) {
			throw new IOException(remoteId + " channel is null");
		}
		ByteBuf call = channel.alloc().buffer();
		assembleRequest(opCode, sessionId, sessionTick, index, param.getComponentId(), param.getRequestBuffer(), param.getRequestLength(), call);
		channel.writeAndFlush(call);
	}

	// public void invokeRequest(int opCode, RemotingParam param, long timeout) throws IOException {
	// invokeRequest(opCode, RemoteContextRingBuffer.NULL_SESSION_ID, ONCE_INVOKE_INDEX, param, timeout);
	// }

	public void registerPlugin(Handler name, Supplier<ChannelHandler> supplier) {
		this.plugins.put(name, supplier);
	}

	public void release(int sessionId, long sessionTick) {
		remoteContext.release(sessionId, sessionTick);
	}
}
