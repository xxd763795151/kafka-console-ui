package com.xuxd.kafka.console.beans;

import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-19 17:07:50
 **/
@Data
public class ReplicaAssignment {

    private long version = 1L;

    private List<Partition> partitions;

    @Data
    static class Partition {
        private String topic;

        private int partition;

        private List<Integer> replicas;
    }
}
