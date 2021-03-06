package com.xuxd.kafka.console.beans;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-19 23:28:31
 **/
@Data
public class SendMessage {

    private String topic;

    private int partition;

    private String key;

    private String body;

    private int num;

    private long offset;
}
