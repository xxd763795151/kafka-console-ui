package com.xuxd.kafka.console.beans.vo;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-07 14:10:10
 **/
@Data
public class KafkaUserDetailVO {

    private String username;

    private String password;

    private String credentialInfos;

    private String consistencyDescription;
}
