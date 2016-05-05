package io.zeymo.network.context;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.EventExecutor;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created By Zeymo at 14/11/19 12:23
 */
public class ChannelGroupContext extends DefaultChannelGroup {

	private final Random								random;

	private final List<Integer>							channelIdentities;

	private final ConcurrentHashMap<Integer, Channel>	neuronMap;

	private final ConcurrentHashMap<String, Channel>	clientMap;

/*	private final ChannelFutureListener					remover0	= new ChannelFutureListener() {
																		@Override
																		public void operationComplete(ChannelFuture future) throws Exception {
																			remove(future.channel());
																		}
																	};*/

	public ChannelGroupContext(String name, EventExecutor executor) {
		super(name, executor);
		random = new Random();
		channelIdentities = Collections.synchronizedList(new ArrayList<Integer>());
		neuronMap = new ConcurrentHashMap<Integer, Channel>();
		clientMap = new ConcurrentHashMap<String, Channel>();
	}

	public Channel getServer(int uid) {
		return neuronMap.get(uid);
	}

	public int getMachineId(Channel channel){
		if(channel == null || neuronMap.size() == 0){
			return -999;
		}

		for(int machineId : neuronMap.keySet()){

			if(neuronMap.get(machineId).compareTo(channel) == 0){
				return machineId;
			}
		}
		return -999;
	}

	public Channel getClient(String hostName) {
		return clientMap.get(hostName);
	}

	public void add(int uid, Channel channel) {
		super.add(channel);
		channelIdentities.add(uid);
		neuronMap.put(uid, channel);
		//channel.closeFuture().addListener(remover0);
	}

	public void addClient(Channel channel) {
		clientMap.put(hostNameOf(channel), channel);
		//channel.closeFuture().addListener(remover0);
	}

	public void remove(Channel channel) {

		for (int key : neuronMap.keySet()) {
			if (neuronMap.get(key) == channel) {
				neuronMap.remove(key);
				channelIdentities.remove((Integer) key);
				super.remove(channel);
				//channel.closeFuture().removeListener(remover0);
				return;
			}
		}

		for (String hostName : clientMap.keySet()) {
			if (hostName.equals(hostNameOf(channel))) {
				clientMap.remove(hostName);
				super.remove(channel);
				//channel.closeFuture().removeListener(remover0);
				return;
			}
		}
	}

	public boolean contains(int uid) {
		return neuronMap.containsKey(uid);
	}

	public boolean contains(String hostName) {
		return clientMap.containsKey(hostName);
	}

	public Set<String> clientKeySet() {
		return clientMap.keySet();
	}

	@Override
	public ChannelGroupFuture close() {
		neuronMap.clear();
		clientMap.clear();
		channelIdentities.clear();
		return super.close().awaitUninterruptibly();
	}

	@Override
	public int size() {
		return neuronMap.size() + clientMap.size();
	}

	public int balance() {
		if (channelIdentities.size() == 0) {
			// 没有可用的服务器可以调用
			return -1;
		}
		return channelIdentities.get(random.nextInt(channelIdentities.size()));
	}

	public static String hostNameOf(Channel channel) {
		return ((InetSocketAddress) channel.remoteAddress()).getHostString();
	}

	@Override
	public String toString() {
		return "server channels = " + neuronMap.toString() + "\nclient channels = " + clientMap.toString() + "\nchannelIdentities = " + channelIdentities.toString();
	}
}
