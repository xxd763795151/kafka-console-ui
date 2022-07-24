package com.xuxd.kafka.console.service.impl;

import com.google.gson.Gson;
import com.xuxd.kafka.console.beans.ReplicaAssignment;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.TopicThrottleSwitch;
import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.beans.vo.TopicDescriptionVO;
import com.xuxd.kafka.console.beans.vo.TopicPartitionVO;
import com.xuxd.kafka.console.service.TopicService;
import com.xuxd.kafka.console.utils.GsonUtil;
import kafka.console.MessageConsole;
import kafka.console.TopicConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    @Autowired
    private MessageConsole messageConsole;

    private Gson gson = GsonUtil.INSTANCE.get();

    @Override public ResponseData getTopicNameList(boolean internal) {
        return ResponseData.create().data(topicConsole.getTopicNameList(internal)).success();
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

    @Override public ResponseData deleteTopics(Collection<String> topics) {
        Tuple2<Object, String> tuple2 = topicConsole.deleteTopics(topics);
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
        // computer the valid time range.
        Map<TopicPartition, Object> beginOffsetTable = new HashMap<>();
        Map<TopicPartition, Object> endOffsetTable = new HashMap<>();
        Map<Integer, TopicPartition> partitionCache = new HashMap<>();

        for (TopicPartitionVO partitionVO : voList) {
            long begin = beginTable.get(partitionVO.getPartition());
            long end = endTable.get(partitionVO.getPartition());
            partitionVO.setBeginOffset(begin);
            partitionVO.setEndOffset(end);
            partitionVO.setDiff(end - begin);

            if (begin != end) {
                TopicPartition partition = new TopicPartition(topic, partitionVO.getPartition());
                partitionCache.put(partitionVO.getPartition(), partition);
                beginOffsetTable.put(partition, begin);
                endOffsetTable.put(partition, end - 1); // end must < endOff
            } else {
                partitionVO.setBeginTime(-1L);
                partitionVO.setEndTime(-1L);
            }
        }

        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> beginRecordMap = messageConsole.searchBy(beginOffsetTable);
        Map<TopicPartition, ConsumerRecord<byte[], byte[]>> endRecordMap = messageConsole.searchBy(endOffsetTable);

        for (TopicPartitionVO partitionVO : voList) {
            if (partitionVO.getBeginTime() != -1L) {
                TopicPartition partition = partitionCache.get(partitionVO.getPartition());
                partitionVO.setBeginTime(beginRecordMap.containsKey(partition) ? beginRecordMap.get(partition).timestamp() : -1L);
                partitionVO.setEndTime(endRecordMap.containsKey(partition) ? endRecordMap.get(partition).timestamp() : -1L);
            }
        }

        return ResponseData.create().data(voList).success();
    }

    @Override public ResponseData createTopic(NewTopic topic) {
        Tuple2<Object, String> createResult = topicConsole.createTopic(topic);
        return (boolean) createResult._1 ? ResponseData.create().success() : ResponseData.create().failed(String.valueOf(createResult._2));
    }

    @Override public ResponseData addPartitions(String topic, int addNum, List<List<Integer>> newAssignments) {
        List<TopicDescription> list = topicConsole.getTopicList(Collections.singleton(topic));
        if (list.isEmpty()) {
            return ResponseData.create().failed("topic not exist.");
        }
        TopicDescription topicDescription = list.get(0);

        Map<String, NewPartitions> param = new HashMap<>();
        param.put(topic, (newAssignments.size() > 0 ? NewPartitions.increaseTo(topicDescription.partitions().size() + addNum, newAssignments) :
            NewPartitions.increaseTo(topicDescription.partitions().size() + addNum)));

        Tuple2<Object, String> tuple2 = topicConsole.createPartitions(param);
        boolean success = (boolean) tuple2._1();

        return success ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData getCurrentReplicaAssignment(String topic) {
        Tuple2<Object, String> tuple2 = topicConsole.getCurrentReplicaAssignmentJson(topic);
        boolean success = (boolean) tuple2._1();

        return success ? ResponseData.create().data(gson.fromJson(tuple2._2(), ReplicaAssignment.class)).success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData updateReplicaAssignment(ReplicaAssignment assignment) {
        Tuple2<Object, String> tuple2 = topicConsole.updateReplicas(gson.toJson(assignment), assignment.getInterBrokerThrottle());
        boolean success = (boolean) tuple2._1();
        return success ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override
    public ResponseData configThrottle(String topic, List<Integer> partitions, TopicThrottleSwitch throttleSwitch) {
        Tuple2<Object, String> tuple2 = null;
        switch (throttleSwitch) {
            case ON:
                tuple2 = topicConsole.configThrottle(topic, partitions);
                break;
            case OFF:
                tuple2 = topicConsole.clearThrottle(topic);
                break;
            default:
                throw new IllegalArgumentException("switch is unknown.");
        }
        boolean success = (boolean) tuple2._1();
        return success ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData sendStats(String topic) {
        Calendar calendar = Calendar.getInstance();
        long current = calendar.getTimeInMillis();

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long today = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_MONTH, -1);
        long yesterday = calendar.getTimeInMillis();

        Map<TopicPartition, Long> currentOffset = topicConsole.getOffsetForTimestamp(topic, current);
        Map<TopicPartition, Long> todayOffset = topicConsole.getOffsetForTimestamp(topic, today);
        Map<TopicPartition, Long> yesterdayOffset = topicConsole.getOffsetForTimestamp(topic, yesterday);

        Map<String, Object> res = new HashMap<>();

        // 昨天的消息数是今天减去昨天的
        AtomicLong yesterdayTotal = new AtomicLong(0L), todayTotal = new AtomicLong(0L);
        Map<Integer, Long> yesterdayDetail = new HashMap<>(), todayDetail = new HashMap<>();
        todayOffset.forEach(((partition, aLong) -> {
            Long last = yesterdayOffset.get(partition);
            long diff = last == null ? aLong : aLong - last;
            yesterdayDetail.put(partition.partition(), diff);
            yesterdayTotal.addAndGet(diff);
        }));
        currentOffset.forEach(((partition, aLong) -> {
            Long last = todayOffset.get(partition);
            long diff = last == null ? aLong : aLong - last;
            todayDetail.put(partition.partition(), diff);
            todayTotal.addAndGet(diff);
        }));

        Map<String, Object> yes = new HashMap<>(), to = new HashMap<>();
        yes.put("detail", convertList(yesterdayDetail));
        yes.put("total", yesterdayTotal.get());
        to.put("detail", convertList(todayDetail));
        to.put("total", todayTotal.get());

        res.put("yesterday", yes);
        res.put("today", to);
        // 今天的消息数是现在减去今天0时的
        return ResponseData.create().data(res).success();
    }

    private List<Map<String, Object>> convertList(Map<Integer, Long> source) {
        List<Map<String, Object>> collect = source.entrySet().stream().map(entry -> {
            Map<String, Object> map = new HashMap<>(3, 1.0f);
            map.put("partition", entry.getKey());
            map.put("num", entry.getValue());
            return map;
        }).collect(Collectors.toList());

        return collect;
    }
}
