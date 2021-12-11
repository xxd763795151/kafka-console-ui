package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 14:19:35
 **/
@Data
public class ConsumerRecordVO {

    private String topic;

    private int partition;

    private long offset;

    private long timestamp;

    public static ConsumerRecordVO fromConsumerRecord(ConsumerRecord record) {
        ConsumerRecordVO vo = new ConsumerRecordVO();
        vo.setTopic(record.topic());
        vo.setPartition(record.partition());
        vo.setOffset(record.offset());
        vo.setTimestamp(record.timestamp());

        return vo;
    }
}
