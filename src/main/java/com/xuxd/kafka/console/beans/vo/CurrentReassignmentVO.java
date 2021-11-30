package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.TopicPartition;
import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-30 16:03:41
 **/
@Data
public class CurrentReassignmentVO {

    private final String topic;

    private final int partition;

    private final List<Integer> replicas;

    private final List<Integer> addingReplicas;

    private final List<Integer> removingReplicas;
}
