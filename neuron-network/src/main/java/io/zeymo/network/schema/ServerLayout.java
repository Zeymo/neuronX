package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

/**
 * Created By Zeymo at 15/1/15 17:30
 */
public class ServerLayout implements JsonPropertyConfigurable {

    private int bossThreadNum;

    private int workThreadNum;

    @Override
    public void configure(JsonProperties properties) {
        this.bossThreadNum = properties.getIntegerNotNull("boss-thread-num");
        this.workThreadNum = properties.getIntegerNotNull("work-thread-num");
    }

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public int getWorkThreadNum() {
        return workThreadNum;
    }
}
