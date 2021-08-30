package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.AclEntry;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-30 19:37:47
 **/
@Data
public class DeleteAclDTO {

    private String username;

    public AclEntry toUserEntry() {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(username);

        return entry;
    }
}
