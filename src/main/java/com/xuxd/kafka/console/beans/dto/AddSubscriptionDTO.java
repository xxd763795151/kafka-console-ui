package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-21 15:43:56
 **/
@Data
public class AddSubscriptionDTO {

    private String groupId;

    private String topic;
}
