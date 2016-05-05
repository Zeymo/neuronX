package io.zeymo.network.schema;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class MatrixLayout {

	public static class MatrixNode {
		private final int		id;
		private final int		row;
		private final int		column;
		private final String	hostName;

		public String getConnectionName() {
			return hostName;
		}

		public MatrixNode(int column, int row, int id, String hostName) {
			this.hostName = hostName;
			this.id = id;
			this.row = row;
			this.column = column;
		}

		public int getRow() {
			return row;
		}

		public int getColumn() {
			return column;
		}

		public int getId() {
			return id;
		}

		public String getHostName() {
			return hostName;
		}

	}

	public static ArrayList<String> getLocalIpList() {
		ArrayList<String> hostList = new ArrayList<String>();
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			InetAddress host = null;
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();
				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					host = addresses.nextElement();
					if (host != null && host instanceof Inet4Address) {
						hostList.add(host.getHostAddress());
						hostList.add(host.getHostName());
					}
				}
			}
		} catch (SocketException e) {
			// 机器上没有装网卡
			throw new RuntimeException("unable to get local addresses, check network configuration.");
		}
		return hostList;
	}

	private MatrixNode				localNode;
	private ArrayList<MatrixNode>	nodeList;

	public MatrixNode getNode(String hostName) {
		for (MatrixNode node : nodeList) {
			if (node.getHostName().equals(hostName)) {
				return node;
			}
		}
		return null;
	}

	public int getNodeCount() {
		return this.nodeList.size();
	}

	public MatrixNode getLocalNode() {
		return localNode;
	}

	public ArrayList<MatrixNode> getNodeList() {
		return nodeList;
	}

	public void setLocalNode(MatrixNode localNode) {
		this.localNode = localNode;
	}

	public void setNodeList(ArrayList<MatrixNode> nodeList) {
		this.nodeList = nodeList;
	}
}
