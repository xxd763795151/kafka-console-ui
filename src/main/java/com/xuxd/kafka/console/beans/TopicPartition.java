package com.xuxd.kafka.console.beans;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-26 20:32:15
 **/
@Data
public class TopicPartition implements Comparable {

    private final String topic;

    private final int partition;

    @Override public int compareTo(Object o) {
        if (o == null) {
            return -1;
        }
        TopicPartition other = (TopicPartition) o;
        if (!this.topic.equals(other.getTopic())) {
            return this.compareTo(other);
        }

        return this.partition - other.partition;
    }
}
