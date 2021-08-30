package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.AclEntry;
import lombok.Data;
import org.apache.kafka.common.resource.ResourceType;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-30 16:28:23
 **/
@Data
public class ProducerAuthDTO {

    private String topic;

    private String username;

    public AclEntry toEntry() {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(username);
        entry.setName(topic);
        entry.setResourceType(ResourceType.TOPIC.name());
        return entry;
    }
}
