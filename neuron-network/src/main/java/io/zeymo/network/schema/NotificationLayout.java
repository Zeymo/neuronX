package io.zeymo.network.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;

/**
 * Created By Zeymo at 15/1/22 14:51
 */
public class NotificationLayout implements JsonPropertyConfigurable {

    private String dataId;

    private String groupId;

    private String subscriberName;

    private String publisherName;

    @Override
    public void configure(JsonProperties properties) {
        this.dataId = properties.getStringNotNull("data-id");
        this.groupId = properties.getStringNotNull("group-id");
        this.subscriberName = properties.getStringNotNull("subscriber-name");
        this.publisherName = properties.getStringNotNull("publisher-name");
    }

    public String getDataId() {
        return dataId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public String getPublisherName() {
        return publisherName;
    }
}
