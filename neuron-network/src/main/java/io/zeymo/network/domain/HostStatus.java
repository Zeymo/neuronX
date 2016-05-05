package io.zeymo.network.domain;

public class HostStatus {

	private int		id;

	private String	hostName;

	private boolean	onService;

	public HostStatus(String hostName, int id, boolean onService) {
		this.hostName = hostName;
		this.id = id;
		this.onService = onService;
	}

	public HostStatus() {

	}

	public int getId() {
		return id;
	}

	public String getHostName() {
		return hostName;
	}

	public boolean isOnService() {
		return onService;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setOnService(boolean onService) {
		this.onService = onService;
	}

}
