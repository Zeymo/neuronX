package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;
import io.zeymo.commons.properties.JsonPropertyUtils;

/**
 * Created By Zeymo at 15/1/15 17:29
 */
public class NetworkLayout implements JsonPropertyConfigurable {

    private int port;

    private int timeout;

    private int remoteContextSize;

    private int channelPoolSize;

    private ProtocolLayout protocolLayout;

    private FileTransferLayout fileTransferLayout;

    private ClientLayout clientLayout;

    private ServerLayout  serverLayout;

    private NotificationLayout  notificationLayout;

    @Override
    public void configure(JsonProperties properties) {
        this.port = properties.getIntegerNotNull("port");
        this.timeout = properties.getIntegerNotNull("timeout");
        this.remoteContextSize = properties.getIntegerNotNull("remote-context-size");
        this.channelPoolSize = properties.getInteger("channel-pool-size");
        this.protocolLayout = JsonPropertyUtils.newInstance(ProtocolLayout.class, properties.getSubProperties("protocol-layout"));
        this.fileTransferLayout = JsonPropertyUtils.newInstance(FileTransferLayout.class,properties.getSubProperties("file-transfer-layout"));
        this.clientLayout = JsonPropertyUtils.newInstance(ClientLayout.class,properties.getSubProperties("client-layout"));
        this.serverLayout = JsonPropertyUtils.newInstance(ServerLayout.class,properties.getSubProperties("server-layout"));
        this.notificationLayout = JsonPropertyUtils.newInstance(NotificationLayout.class,properties.getSubProperties("notification-layout"));
    }

    public int getPort() {
        return port;
    }

    public int getTimeout() {
        return timeout;
    }

    public int getRemoteContextSize() {
        return remoteContextSize;
    }

    public ProtocolLayout getProtocolLayout() {
        return protocolLayout;
    }

    public FileTransferLayout getFileTransferLayout() {
        return fileTransferLayout;
    }

    public ClientLayout getClientLayout() {
        return clientLayout;
    }

    public ServerLayout getServerLayout() {
        return serverLayout;
    }

    public NotificationLayout getNotificationLayout() {
        return notificationLayout;
    }

    public int getChannelPoolSize() {
        return channelPoolSize;
    }
}
