package com.xuxd.kafka.console.beans.vo;

import kafka.console.ConsumerConsole;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-12 17:41:44
 **/
@Data
public class ConsumerDetailVO implements Comparable {

    private String topic = "";

    private int partition;

    private String groupId = "";

    private long consumerOffset = 0L;

    private long logEndOffset = 0L;

    private long lag = 0L;

    private String consumerId = "";

    private String clientId = "";

    private String host = "";

    public static ConsumerDetailVO from(ConsumerConsole.TopicPartitionConsumeInfo info) {
        ConsumerDetailVO vo = new ConsumerDetailVO();
        vo.topic = info.topicPartition().topic();
        vo.partition = info.topicPartition().partition();
        if (StringUtils.isNotEmpty(info.getGroupId())) {
            vo.groupId = info.getGroupId();
        }

        if (StringUtils.isNotEmpty(info.consumerId())) {
            vo.consumerId = info.consumerId();
        }

        if (StringUtils.isNotEmpty(info.clientId())) {
            vo.clientId = info.clientId();
        }

        if (StringUtils.isNotEmpty(info.host())) {
            vo.host = info.host();
        }
        vo.consumerOffset = info.consumerOffset();
        vo.logEndOffset = info.logEndOffset();
        vo.lag = info.lag();
        return vo;
    }

    @Override public int compareTo(Object o) {

        ConsumerDetailVO that = (ConsumerDetailVO) o;

        if (!this.groupId.equals(that.groupId)) {
            return this.groupId.compareTo(that.groupId);
        }
        if (!this.topic.equals(that.topic)) {
            return this.topic.compareTo(that.topic);
        }

        return this.partition - that.partition;
    }
}
