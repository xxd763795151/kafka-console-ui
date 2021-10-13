package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.beans.vo.TopicDescriptionVO;
import com.xuxd.kafka.console.beans.vo.TopicPartitionVO;
import com.xuxd.kafka.console.service.TopicService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.TopicConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:02:56
 **/
@Slf4j
@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicConsole topicConsole;

    @Override public ResponseData getTopicNameList() {
        return ResponseData.create().data(topicConsole.getTopicNameList(true)).success();
    }

    @Override public ResponseData getTopicList(String topic, TopicType type) {
        Set<String> topicSet = new HashSet<>();
        switch (type) {
            case SYSTEM:
                Set<String> internalTopicSet = topicConsole.getInternalTopicNameList();
                if (StringUtils.isEmpty(topic)) {
                    topicSet.addAll(internalTopicSet);
                } else {
                    if (internalTopicSet.contains(topic)) {
                        topicSet.add(topic);
                    } else {
                        return ResponseData.create().data(Collections.emptyList()).success();
                    }
                }
                break;
            case NORMAL:
                Set<String> internalTopicS = topicConsole.getInternalTopicNameList();
                if (internalTopicS.contains(topic)) {
                    return ResponseData.create().data(Collections.emptyList()).success();
                }
                topicSet.addAll(StringUtils.isEmpty(topic) ? topicConsole.getTopicNameList(false) : Collections.singleton(topic));
                break;
            default:
                topicSet.addAll(StringUtils.isEmpty(topic) ? topicConsole.getTopicNameList(true) : Collections.singleton(topic));
                break;
        }
        List<TopicDescription> topicDescriptions = topicConsole.getTopicList(topicSet);
        topicDescriptions.sort(Comparator.comparing(TopicDescription::name));

        return ResponseData.create().data(topicDescriptions.stream().map(d -> TopicDescriptionVO.from(d))).success();
    }

    @Override public ResponseData deleteTopic(String topic) {
        Tuple2<Object, String> tuple2 = topicConsole.deleteTopic(topic);
        return (Boolean) tuple2._1 ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2);
    }

    @Override public ResponseData getTopicPartitionInfo(String topic) {
        List<TopicDescription> list = topicConsole.getTopicList(Collections.singleton(topic));
        if (list.isEmpty()) {
            return ResponseData.create().success();
        }
        TopicDescription topicDescription = list.get(0);
        List<TopicPartitionInfo> topicPartitionInfos = topicDescription.partitions();
        List<TopicPartitionVO> voList = topicPartitionInfos.stream().map(TopicPartitionVO::from).collect(Collectors.toList());
        List<TopicPartition> partitions = topicPartitionInfos.stream().map(p -> new TopicPartition(topic, p.partition())).collect(Collectors.toList());

        Tuple2<Map<TopicPartition, Object>, Map<TopicPartition, Object>> mapTuple2 = topicConsole.getTopicOffset(topic, partitions);
        Map<Integer, Long> beginTable = new HashMap<>(), endTable = new HashMap<>();

        mapTuple2._1().forEach((k, v) -> {
            beginTable.put(k.partition(), (Long) v);
        });
        mapTuple2._2().forEach((k, v) -> {
            endTable.put(k.partition(), (Long) v);
        });

        for (TopicPartitionVO partitionVO : voList) {
            long begin = beginTable.get(partitionVO.getPartition());
            long end = endTable.get(partitionVO.getPartition());
            partitionVO.setBeginOffset(begin);
            partitionVO.setEndOffset(end);
            partitionVO.setDiff(end - begin);
        }
        return ResponseData.create().data(voList).success();
    }

    @Override public ResponseData createTopic(NewTopic topic) {
        Tuple2<Object, String> createResult = topicConsole.createTopic(topic);
        return (boolean) createResult._1 ? ResponseData.create().success() : ResponseData.create().failed(String.valueOf(createResult._2));
    }
}
