package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.TopicPartition;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import org.apache.kafka.clients.admin.MemberDescription;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-26 20:02:59
 **/
@Data
public class ConsumerMemberVO {

    private String memberId;
    private String groupInstanceId;
    private String clientId;
    private String host;
    private List<TopicPartition> partitions;

    public static ConsumerMemberVO from(MemberDescription description) {
        ConsumerMemberVO vo = new ConsumerMemberVO();
        vo.setMemberId(description.consumerId());
        vo.setGroupInstanceId(description.groupInstanceId().orElse(""));
        vo.setClientId(description.clientId());
        vo.setHost(description.host());
        List<TopicPartition> collect = description.assignment().topicPartitions().stream().map(t -> new TopicPartition(t.topic(), t.partition())).collect(Collectors.toList());
        collect.sort(Comparator.naturalOrder());
        vo.setPartitions(collect);

        return vo;
    }

}
