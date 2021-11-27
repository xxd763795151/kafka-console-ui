package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.enums.TopicThrottleSwitch;
import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-26 15:33:37
 **/
@Data
public class TopicThrottleDTO {

    private String topic;

    private List<Integer> partitions;

    private TopicThrottleSwitch operation;
}
