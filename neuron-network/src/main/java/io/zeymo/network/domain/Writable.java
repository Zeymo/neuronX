package io.zeymo.network.domain;

import io.netty.buffer.ByteBuf;

/**
 * Created By Zeymo at 14/11/20 14:10
 */
public interface Writable {

    void write(ByteBuf out);

    void readFields(ByteBuf in);
}
