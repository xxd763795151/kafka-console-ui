package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.AclEntry;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-31 17:18:40
 **/
@Data
public class QueryAclDTO {

    private String resourceType;

    private String username = null;

    private String resourceName = null;

    public AclEntry toEntry() {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(username);
        entry.setResourceType(resourceType);
        entry.setName(resourceName);

        return entry;
    }

}
