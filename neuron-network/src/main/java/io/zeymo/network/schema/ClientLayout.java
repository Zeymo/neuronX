package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

/**
 * Created By Zeymo at 15/1/15 17:30
 */
public class ClientLayout implements JsonPropertyConfigurable {

    private int workThreadNum;

    private int connectTimeout;

    private int reconnectDelay;

    @Override
    public void configure(JsonProperties properties) {
        this.workThreadNum = properties.getIntegerNotNull("work-thread-num");
        this.connectTimeout = properties.getIntegerNotNull("connect-time-out");
        this.reconnectDelay = properties.getIntegerNotNull("reconnect-delay");
    }

    public int getWorkThreadNum() {
        return workThreadNum;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public int getReconnectDelay() {
        return reconnectDelay;
    }
}
