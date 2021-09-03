package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.AclEntry;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-03 19:07:11
 **/
@Data
public class AddAuthDTO {

    private String resourceType;

    private String resourceName = null;

    private String username = null;

    private String host;

    private String operation;

    private String permissionType;

    public AclEntry toAclEntry() {
        AclEntry entry = new AclEntry();
        entry.setResourceType(resourceType);
        entry.setName(resourceName);
        entry.setPrincipal(username);
        entry.setHost(host);
        entry.setOperation(operation);
        entry.setPermissionType(permissionType);
        return entry;
    }
}
