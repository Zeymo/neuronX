package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

/**
 * Created By Zeymo at 15/1/15 17:30
 */
public class ProtocolLayout implements JsonPropertyConfigurable {

    private int maxFrameLength;

    private int lengthFieldOffset;

    private int lengthFieldLength;

    private int lengthAdjustment;

    private int initialBytesToStrip;

    private int pingScheduleTime;

    private int pingTimeout;

    @Override
    public void configure(JsonProperties properties) {
        this.maxFrameLength = properties.getIntegerNotNull("max-frame-length");
        this.lengthFieldOffset = properties.getIntegerNotNull("length-filed-offset");
        this.lengthFieldLength = properties.getIntegerNotNull("length-filed-length");
        this.lengthAdjustment = properties.getIntegerNotNull("length-adjustment");
        this.initialBytesToStrip = properties.getIntegerNotNull("initial-bytes-to-strip");
        this.pingScheduleTime = properties.getIntegerNotNull("ping-schedule-time");
        this.pingTimeout = properties.getIntegerNotNull("ping-time-out");
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public int getLengthFieldOffset() {
        return lengthFieldOffset;
    }

    public int getLengthFieldLength() {
        return lengthFieldLength;
    }

    public int getLengthAdjustment() {
        return lengthAdjustment;
    }

    public int getInitialBytesToStrip() {
        return initialBytesToStrip;
    }

    public int getPingScheduleTime() {
        return pingScheduleTime;
    }

    public int getPingTimeout() {
        return pingTimeout;
    }
}
