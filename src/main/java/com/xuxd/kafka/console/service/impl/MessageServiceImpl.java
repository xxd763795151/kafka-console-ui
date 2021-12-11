package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.vo.ConsumerRecordVO;
import com.xuxd.kafka.console.service.MessageService;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.MessageConsole;
import kafka.console.TopicConsole;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:43:44
 **/
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageConsole messageConsole;

    @Autowired
    private TopicConsole topicConsole;

    @Override public ResponseData searchByTime(QueryMessage queryMessage) {
        int maxNums = 10000;

        Set<TopicPartition> partitions = getPartitions(queryMessage);
        List<ConsumerRecord<byte[], byte[]>> records = messageConsole.searchBy(partitions, queryMessage.getStartTime(), queryMessage.getEndTime(), maxNums);
        List<ConsumerRecordVO> vos = records.stream().filter(record -> record.timestamp() <= queryMessage.getEndTime())
            .map(ConsumerRecordVO::fromConsumerRecord).collect(Collectors.toList());
        Map<String, Object> res = new HashMap<>();
        res.put("maxNum", maxNums);
        res.put("realNum", vos.size());
        res.put("data", vos.subList(0, Math.min(maxNums, vos.size())));
        return ResponseData.create().data(res).success();
    }

    @Override public ResponseData searchByOffset(QueryMessage queryMessage) {
        Set<TopicPartition> partitions = getPartitions(queryMessage);
        Map<TopicPartition, Object> offsetTable = new HashMap<>();
        partitions.forEach(tp -> {
            offsetTable.put(tp, queryMessage.getOffset());
        });
        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> recordMap = messageConsole.searchBy(offsetTable);

        return ResponseData.create().data(recordMap.values().stream().map(ConsumerRecordVO::fromConsumerRecord).collect(Collectors.toList())).success();
    }

    private Set<TopicPartition> getPartitions(QueryMessage queryMessage) {
        Set<TopicPartition> partitions = new HashSet<>();
        if (queryMessage.getPartition() != -1) {
            partitions.add(new TopicPartition(queryMessage.getTopic(), queryMessage.getPartition()));
        } else {
            List<TopicDescription> list = topicConsole.getTopicList(Collections.singleton(queryMessage.getTopic()));
            if (CollectionUtils.isEmpty(list)) {
                throw new IllegalArgumentException("Can not find topic info.");
            }
            Set<TopicPartition> set = list.get(0).partitions().stream()
                .map(tp -> new TopicPartition(queryMessage.getTopic(), tp.partition())).collect(Collectors.toSet());
            partitions.addAll(set);
        }
        return partitions;
    }
}
