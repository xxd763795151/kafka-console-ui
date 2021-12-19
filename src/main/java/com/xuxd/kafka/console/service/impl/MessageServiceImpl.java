package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.SendMessage;
import com.xuxd.kafka.console.beans.vo.ConsumerRecordVO;
import com.xuxd.kafka.console.beans.vo.MessageDetailVO;
import com.xuxd.kafka.console.service.ConsumerService;
import com.xuxd.kafka.console.service.MessageService;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.ConsumerConsole;
import kafka.console.MessageConsole;
import kafka.console.TopicConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.BytesDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.DoubleDeserializer;
import org.apache.kafka.common.serialization.FloatDeserializer;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:43:44
 **/
@Slf4j
@Service
public class MessageServiceImpl implements MessageService, ApplicationContextAware {

    @Autowired
    private MessageConsole messageConsole;

    @Autowired
    private TopicConsole topicConsole;

    @Autowired
    private ConsumerConsole consumerConsole;

    private ApplicationContext applicationContext;

    private Map<String, Deserializer> deserializerDict = new HashMap<>();

    {
        deserializerDict.put("ByteArray", new ByteArrayDeserializer());
        deserializerDict.put("Integer", new IntegerDeserializer());
        deserializerDict.put("String", new StringDeserializer());
        deserializerDict.put("Float", new FloatDeserializer());
        deserializerDict.put("Double", new DoubleDeserializer());
        deserializerDict.put("Byte", new BytesDeserializer());
    }

    public static String defaultDeserializer = "String";

    @Override public ResponseData searchByTime(QueryMessage queryMessage) {
        int maxNums = 10000;

        Set<TopicPartition> partitions = getPartitions(queryMessage);
        long startTime = System.currentTimeMillis();
        List<ConsumerRecord<byte[], byte[]>> records = messageConsole.searchBy(partitions, queryMessage.getStartTime(), queryMessage.getEndTime(), maxNums);
        log.info("search message by time, cost time: {}", (System.currentTimeMillis() - startTime));
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

            // 为了尽量保持代码好看，不直接注入另一个service层的实现类了
            Set<String> groupIds = applicationContext.getBean(ConsumerService.class).getSubscribedGroups(record.topic()).getData();
            Collection<ConsumerConsole.TopicPartitionConsumeInfo> consumerDetail = consumerConsole.getConsumerDetail(groupIds);

            List<MessageDetailVO.ConsumerVO> consumerVOS = new LinkedList<>();
            consumerDetail.forEach(consumerInfo -> {
                if (consumerInfo.topicPartition().equals(new TopicPartition(record.topic(), record.partition()))) {
                    MessageDetailVO.ConsumerVO consumerVO = new MessageDetailVO.ConsumerVO();
                    consumerVO.setGroupId(consumerInfo.getGroupId());
                    consumerVO.setStatus(consumerInfo.getConsumerOffset() < record.offset() ? "unconsume" : "consumed");
                    consumerVOS.add(consumerVO);
                }
            });

            vo.setConsumers(consumerVOS);
            return ResponseData.create().data(vo).success();
        }
        return ResponseData.create().failed("Not found message detail.");
    }

    @Override public ResponseData deserializerList() {
        return ResponseData.create().data(deserializerDict.keySet()).success();
    }

    @Override public ResponseData send(SendMessage message) {
        messageConsole.send(message.getTopic(), message.getPartition(), message.getKey(), message.getBody(), message.getNum());
        return ResponseData.create().success();
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

    @Override public void setApplicationContext(ApplicationContext context) throws BeansException {
        this.applicationContext = context;
    }
}
