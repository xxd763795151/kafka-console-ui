package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.vo.ConsumerRecordVO;
import com.xuxd.kafka.console.beans.vo.MessageDetailVO;
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
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.FloatDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
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

    private Map<String, Deserializer> deserializerDict = new HashMap<>();

    {
        deserializerDict.put("Integer", new IntegerDeserializer());
        deserializerDict.put("String", new StringDeserializer());
        deserializerDict.put("Float", new FloatDeserializer());
        deserializerDict.put("Double", new DoubleDeserializer());
    }

    public static String defaultDeserializer = "String";

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
        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> recordMap = searchRecordByOffset(queryMessage);

        return ResponseData.create().data(recordMap.values().stream().map(ConsumerRecordVO::fromConsumerRecord).collect(Collectors.toList())).success();
    }

    @Override public ResponseData searchDetail(QueryMessage queryMessage) {
        if (queryMessage.getPartition() == -1) {
            throw new IllegalArgumentException();
        }
        if (StringUtils.isBlank(queryMessage.getKeyDeserializer())) {
            queryMessage.setKeyDeserializer(defaultDeserializer);
        }

        if (StringUtils.isBlank(queryMessage.getValueDeserializer())) {
            queryMessage.setValueDeserializer(defaultDeserializer);
        }

        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> recordMap = searchRecordByOffset(queryMessage);
        ConsumerRecord<byte[], byte[]> record = recordMap.get(new TopicPartition(queryMessage.getTopic(), queryMessage.getPartition()));
        if (record != null) {
            MessageDetailVO vo = new MessageDetailVO();
            vo.setTopic(record.topic());
            vo.setPartition(record.partition());
            vo.setOffset(record.offset());
            vo.setTimestamp(record.timestamp());
            vo.setTimestampType(record.timestampType().name());
            try {
                vo.setKey(deserializerDict.get(queryMessage.getKeyDeserializer()).deserialize(queryMessage.getTopic(), record.key()));
            } catch (Exception e) {
                vo.setKey("KeyDeserializer Error: " + e.getMessage());
            }
            try {
                vo.setValue(deserializerDict.get(queryMessage.getValueDeserializer()).deserialize(queryMessage.getTopic(), record.value()));
            } catch (Exception e) {
                vo.setValue("ValueDeserializer Error: " + e.getMessage());
            }

            record.headers().forEach(header -> {
                MessageDetailVO.HeaderVO headerVO = new MessageDetailVO.HeaderVO();
                headerVO.setKey(header.key());
                headerVO.setValue(new String(header.value()));
                vo.getHeaders().add(headerVO);
            });

            return ResponseData.create().data(vo).success();
        }
        return ResponseData.create().failed("Not found message detail.");
    }

    @Override public ResponseData deserializerList() {
        return ResponseData.create().data(deserializerDict.keySet()).success();
    }

    private Map<TopicPartition, ConsumerRecord<byte[], byte[]>> searchRecordByOffset(QueryMessage queryMessage) {
        Set<TopicPartition> partitions = getPartitions(queryMessage);

        Map<TopicPartition, Object> offsetTable = new HashMap<>();
        partitions.forEach(tp -> {
            offsetTable.put(tp, queryMessage.getOffset());
        });
        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> recordMap = messageConsole.searchBy(offsetTable);
        return recordMap;
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
