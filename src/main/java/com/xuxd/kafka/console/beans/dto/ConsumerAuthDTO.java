package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.AclEntry;
import lombok.Data;
import org.apache.kafka.common.resource.ResourceType;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-30 16:28:47
 **/
@Data
public class ConsumerAuthDTO {

    private String topic;

    private String groupId;

    private String username;

    public AclEntry toTopicEntry() {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(username);
        entry.setName(topic);
        entry.setResourceType(ResourceType.TOPIC.name());
        return entry;
    }

    public AclEntry toGroupEntry() {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(username);
        entry.setName(groupId);
        entry.setResourceType(ResourceType.GROUP.name());
        return entry;
    }
}
