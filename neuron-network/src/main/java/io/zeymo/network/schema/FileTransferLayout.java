package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

/**
 * Created By Zeymo at 15/1/15 17:30
 */
public class FileTransferLayout implements JsonPropertyConfigurable {

    private int maxNumOfChunk;

    private int chunkSize;

    private int inputBufferSize;

    private int outputBufferSize;

    private int latchTimeout;

    @Override
    public void configure(JsonProperties properties) {
        this.maxNumOfChunk = properties.getIntegerNotNull("max-num-of-chunk");
        this.chunkSize = properties.getIntegerNotNull("chunk-size");
        this.inputBufferSize = properties.getIntegerNotNull("input-buffer-size");
        this.outputBufferSize = properties.getIntegerNotNull("output-buffer-size");
        this.latchTimeout = properties.getIntegerNotNull("latch-timeout");
    }

    public int getMaxNumOfChunk() {
        return maxNumOfChunk;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getOutputBufferSize() {
        return outputBufferSize;
    }

    public int getLatchTimeout() {
        return latchTimeout;
    }
}
