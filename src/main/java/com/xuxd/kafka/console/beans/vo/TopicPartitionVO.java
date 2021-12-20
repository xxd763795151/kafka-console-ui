package com.xuxd.kafka.console.beans.vo;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartitionInfo;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-23 15:15:29
 **/
@Data
public class TopicPartitionVO {

    private int partition;

    private String leader;

    private List<String> replicas;

    private List<String> isr;

    private long beginOffset;

    private long endOffset;

    private long diff;

    private long beginTime;

    private long endTime;

    public static TopicPartitionVO from(TopicPartitionInfo partitionInfo) {
        TopicPartitionVO partitionVO = new TopicPartitionVO();
        partitionVO.setPartition(partitionInfo.partition());
        partitionVO.setLeader(partitionInfo.leader().toString());
        partitionVO.setReplicas(partitionInfo.replicas().stream().map(node -> node.host() + ":" + node.port() + " (id: " + node.idString() + ")").collect(Collectors.toList()));
        partitionVO.setIsr(partitionInfo.isr().stream().map(Node::idString).collect(Collectors.toList()));
        return partitionVO;
    }
}
