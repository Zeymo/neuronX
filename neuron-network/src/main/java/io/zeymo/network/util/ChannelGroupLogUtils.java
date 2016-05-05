package io.zeymo.network.util;

import io.netty.channel.Channel;
import io.zeymo.network.context.ChannelGroupContext;
import org.slf4j.Logger;

/**
 * Created By Zeymo at 14-9-26 11:22
 */
public class ChannelGroupLogUtils {

    public static void format(Logger log,ChannelGroupContext channels){
        if(channels != null && channels.size() != 0 ){
            log.info("channel group contains " + channels.size() + " channels" + channels.toString());
        }else{
            log.info("channel group is empty");
        }
    }

    public static void onCreate(Logger log,Channel channel,ChannelGroupContext channels){
        log.info("create a new channel " + channel);
        format(log,channels);
    }

    public static void onRemove(Logger log,Channel channel,ChannelGroupContext channels){
        log.info("remove channel " + channel);
        format(log,channels);
    }

    public static void format(ChannelGroupContext channels){
        //if(channels != null && channels.size() != 0 ){
            System.out.println("channel group contains " + channels.size() + " channels" + channels.toString());
        //}else{
            //System.out.println("channel group is empty");
        //}
    }
}
