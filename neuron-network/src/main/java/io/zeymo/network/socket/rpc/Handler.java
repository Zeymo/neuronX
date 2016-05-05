package io.zeymo.network.socket.rpc;

/**
 * Created By Zeymo at 15/1/9 17:26
 */
public enum Handler {
    INITIALIZE("initialize"),
    TIMEOUT("timeout"),
    DECODER("decoder"),
    HEARTBEAT("heartbeat"),
    HANDSHAKE("handshake"),
    FILE("file"),
    RPC("rpc");

    public String name;

    private Handler(String name) {
        this.name = name;
    }
}
