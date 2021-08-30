package com.xuxd.kafka.console.beans;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 19:43:26
 **/
@Data
public class AclUser {

    private String username;

    private String password;
}
