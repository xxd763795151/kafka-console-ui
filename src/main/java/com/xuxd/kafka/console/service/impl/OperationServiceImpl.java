package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.MinOffsetAlignmentDO;
import com.xuxd.kafka.console.beans.vo.CurrentReassignmentVO;
import com.xuxd.kafka.console.beans.vo.OffsetAlignmentVO;
import com.xuxd.kafka.console.dao.MinOffsetAlignmentMapper;
import com.xuxd.kafka.console.service.OperationService;
import com.xuxd.kafka.console.utils.GsonUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.OperationConsole;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.PartitionReassignment;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:12:54
 **/
@Service
public class OperationServiceImpl implements OperationService {

    private Gson gson = GsonUtil.INSTANCE.get();

    @Autowired
    private OperationConsole operationConsole;

    private MinOffsetAlignmentMapper minOffsetAlignmentMapper;

    public OperationServiceImpl(ObjectProvider<MinOffsetAlignmentMapper> minOffsetAlignmentMapper) {
        this.minOffsetAlignmentMapper = minOffsetAlignmentMapper.getIfAvailable();
    }

    @Override public ResponseData syncConsumerOffset(String groupId, String topic, Properties thatProps) {
        QueryWrapper<MinOffsetAlignmentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.eq("topic", topic);
        MinOffsetAlignmentDO alignmentDO = minOffsetAlignmentMapper.selectOne(wrapper);
        if (alignmentDO == null) {
            return ResponseData.create().failed("No min offset info.");
        }

        Map<String, Object> thisOffset = gson.fromJson(alignmentDO.getThisOffset(), Map.class);
        Map<String, Object> thatOffset = gson.fromJson(alignmentDO.getThatOffset(), Map.class);

        Map<TopicPartition, Object> thisMinOffset = new HashMap<>(), thatMinOffset = new HashMap<>();
        thisOffset.forEach((k, v) -> {
            thisMinOffset.put(new TopicPartition(topic, Integer.valueOf(k)), Long.valueOf(v.toString()));
        });
        thatOffset.forEach((k, v) -> {
            thatMinOffset.put(new TopicPartition(topic, Integer.valueOf(k)), Long.valueOf(v.toString()));
        });

        Tuple2<Object, String> tuple2 = operationConsole.syncConsumerOffset(groupId, topic, thatProps, thisMinOffset, thatMinOffset);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData minOffsetAlignment(String groupId, String topic, Properties thatProps) {

        Tuple2<Map<TopicPartition, Object>, Map<TopicPartition, Object>> tuple2 = operationConsole.checkAndFetchOffset(groupId, topic, thatProps);
        Map<TopicPartition, Object> thisMaxOffset = tuple2._1();
        Map<TopicPartition, Object> thatMinOffset = tuple2._2();

        JsonObject thisJson = new JsonObject(), thatJson = new JsonObject();
        thisMaxOffset.forEach((k, v) -> {
            thisJson.addProperty(String.valueOf(k.partition()), v.toString());
        });
        thatMinOffset.forEach((k, v) -> {
            thatJson.addProperty(String.valueOf(k.partition()), v.toString());
        });

        MinOffsetAlignmentDO alignmentDO = new MinOffsetAlignmentDO();
        alignmentDO.setGroupId(groupId);
        alignmentDO.setTopic(topic);

        QueryWrapper<MinOffsetAlignmentDO> wrapper = new QueryWrapper<>();
        wrapper.eq("group_id", groupId);
        wrapper.eq("topic", topic);
        if (minOffsetAlignmentMapper.selectCount(wrapper) > 0) {
            minOffsetAlignmentMapper.delete(wrapper);
        }

        alignmentDO.setThisOffset(thisJson.toString());
        alignmentDO.setThatOffset(thatJson.toString());
        minOffsetAlignmentMapper.insert(alignmentDO);
        return ResponseData.create().success();
    }

    @Override public ResponseData getAlignmentList() {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("update_time");
        List<MinOffsetAlignmentDO> alignmentDOS = minOffsetAlignmentMapper.selectList(wrapper);

        return ResponseData.create().data(alignmentDOS.stream().map(OffsetAlignmentVO::from)).success();
    }

    @Override public ResponseData deleteAlignmentById(Long id) {
        minOffsetAlignmentMapper.deleteById(id);
        return ResponseData.create().success();
    }

    @Override public ResponseData electPreferredLeader(String topic, int partition) {
        Set<TopicPartition> partitions = new HashSet<>();
        if (partition != -1) {
            partitions.add(new TopicPartition(topic, partition));
        } else {

            partitions.addAll(operationConsole.getTopicPartitions(topic));
        }
        Tuple2<Object, String> tuple2 = operationConsole.electPreferredLeader(partitions);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData configThrottle(List<Integer> brokerList, long size) {
        Tuple2<Object, String> tuple2 = operationConsole.modifyInterBrokerThrottle(new HashSet<>(brokerList), size);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData removeThrottle(List<Integer> brokerList) {
        Tuple2<Object, String> tuple2 = operationConsole.clearBrokerLevelThrottles(new HashSet<>(brokerList));

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData currentReassignments() {
        Map<TopicPartition, PartitionReassignment> reassignmentMap = operationConsole.currentReassignments();
        List<CurrentReassignmentVO> vos = reassignmentMap.entrySet().stream().map(entry -> {
            TopicPartition partition = entry.getKey();
            PartitionReassignment reassignment = entry.getValue();
            return new CurrentReassignmentVO(partition.topic(),
                partition.partition(), reassignment.replicas(), reassignment.addingReplicas(), reassignment.removingReplicas());
        }).collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override public ResponseData cancelReassignment(TopicPartition partition) {
        Map<TopicPartition, Throwable> res = operationConsole.cancelPartitionReassignments(Collections.singleton(partition));
        if (!res.isEmpty()) {
            StringBuilder sb = new StringBuilder("Failed: ");
            res.forEach((p, t) -> {
                sb.append(p.toString()).append(": ").append(t.getMessage()).append(System.lineSeparator());
            });
            return ResponseData.create().failed(sb.toString());
        }
        return ResponseData.create().success();
    }

    @Override public ResponseData proposedAssignments(String topic, List<Integer> brokerList) {
        Map<String, Object> params = new HashMap<>();
        params.put("version", 1);
        Map<String, String> topicMap = new HashMap<>(1, 1.0f);
        topicMap.put("topic", topic);
        params.put("topics", Lists.newArrayList(topicMap));
        List<String> list = brokerList.stream().map(String::valueOf).collect(Collectors.toList());
        Map<TopicPartition, List<Object>> assignments = operationConsole.proposedAssignments(gson.toJson(params), StringUtils.join(list, ","));
        List<CurrentReassignmentVO> res = new ArrayList<>(assignments.size());
        assignments.forEach((tp, replicas) -> {
            CurrentReassignmentVO vo = new CurrentReassignmentVO(tp.topic(), tp.partition(),
                replicas.stream().map(x -> (Integer) x).collect(Collectors.toList()), null, null);
            res.add(vo);
        });
        return ResponseData.create().data(res).success();
    }
}
