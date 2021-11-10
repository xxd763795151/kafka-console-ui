package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-10 20:09:20
 **/
@Data
public class ReplicationDTO {

    private String topic;

    private int partition;
}
