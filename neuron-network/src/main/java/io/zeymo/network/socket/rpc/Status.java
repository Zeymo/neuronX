package io.zeymo.network.socket.rpc;

/**
 * Created By Zeymo at 14/12/4 14:34
 */
public enum Status {
    ERROR (0),
    SUCCESS (1);

    public int state;

    private Status(int state) {
        this.state = state;
    }
}
