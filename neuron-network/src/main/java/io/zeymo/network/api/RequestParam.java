package io.zeymo.network.api;

public interface RequestParam {

	public byte[] getRequestBuffer();

	public int getRequestLength();

	public int getComponentId();

	public int getMachineId();

	public byte[] getResponseBuffer();

	public int getResponseLength();

	public void complete(int sessionId, byte[] responseBuffer, int offset, int length);

}
