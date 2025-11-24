package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.xuxd.kafka.console.beans.ConsoleData;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.*;
import com.xuxd.kafka.console.beans.vo.CurrentReassignmentVO;
import com.xuxd.kafka.console.beans.vo.OffsetAlignmentVO;
import com.xuxd.kafka.console.dao.*;
import com.xuxd.kafka.console.service.OperationService;
import com.xuxd.kafka.console.utils.GsonUtil;
import kafka.console.OperationConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.PartitionReassignment;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import scala.Tuple2;

import java.util.*;
import java.util.stream.Collectors;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:12:54
 **/
@Slf4j
@Service
public class OperationServiceImpl implements OperationService {

    private Gson gson = GsonUtil.INSTANCE.get();

    private final OperationConsole operationConsole;

    private MinOffsetAlignmentMapper minOffsetAlignmentMapper;

    private final ClusterInfoMapper clusterInfoMapper;

    private final ClusterRoleRelationMapper clusterRoleRelationMapper;

    private final SysRoleMapper sysRoleMapper;

    private final SysUserMapper sysUserMapper;

    private final KafkaUserMapper kafkaUserMapper;

    public OperationServiceImpl(ObjectProvider<MinOffsetAlignmentMapper> minOffsetAlignmentMapper,
                                OperationConsole operationConsole,
                                ClusterInfoMapper clusterInfoMapper,
                                ClusterRoleRelationMapper clusterRoleRelationMapper,
                                SysRoleMapper sysRoleMapper,
                                SysUserMapper sysUserMapper,
                                KafkaUserMapper kafkaUserMapper) {
        this.minOffsetAlignmentMapper = minOffsetAlignmentMapper.getIfAvailable();
        this.operationConsole = operationConsole;
        this.clusterInfoMapper = clusterInfoMapper;
        this.clusterRoleRelationMapper = clusterRoleRelationMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysUserMapper = sysUserMapper;
        this.kafkaUserMapper = kafkaUserMapper;
    }

    @Override
    public ResponseData syncConsumerOffset(String groupId, String topic, Properties thatProps) {
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

    @Override
    public ResponseData minOffsetAlignment(String groupId, String topic, Properties thatProps) {

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

    @Override
    public ResponseData getAlignmentList() {
        QueryWrapper wrapper = new QueryWrapper();
        wrapper.orderByDesc("update_time");
        List<MinOffsetAlignmentDO> alignmentDOS = minOffsetAlignmentMapper.selectList(wrapper);

        return ResponseData.create().data(alignmentDOS.stream().map(OffsetAlignmentVO::from)).success();
    }

    @Override
    public ResponseData deleteAlignmentById(Long id) {
        minOffsetAlignmentMapper.deleteById(id);
        return ResponseData.create().success();
    }

    @Override
    public ResponseData electPreferredLeader(String topic, int partition) {
        Set<TopicPartition> partitions = new HashSet<>();
        if (partition != -1) {
            partitions.add(new TopicPartition(topic, partition));
        } else {

            partitions.addAll(operationConsole.getTopicPartitions(topic));
        }
        Tuple2<Object, String> tuple2 = operationConsole.electPreferredLeader(partitions);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override
    public ResponseData configThrottle(List<Integer> brokerList, long size) {
        Tuple2<Object, String> tuple2 = operationConsole.modifyInterBrokerThrottle(new HashSet<>(brokerList), size);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override
    public ResponseData removeThrottle(List<Integer> brokerList) {
        Tuple2<Object, String> tuple2 = operationConsole.clearBrokerLevelThrottles(new HashSet<>(brokerList));

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override
    public ResponseData currentReassignments() {
        Map<TopicPartition, PartitionReassignment> reassignmentMap = operationConsole.currentReassignments();
        List<CurrentReassignmentVO> vos = reassignmentMap.entrySet().stream().map(entry -> {
            TopicPartition partition = entry.getKey();
            PartitionReassignment reassignment = entry.getValue();
            return new CurrentReassignmentVO(partition.topic(),
                    partition.partition(), reassignment.replicas(), reassignment.addingReplicas(), reassignment.removingReplicas());
        }).collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override
    public ResponseData cancelReassignment(TopicPartition partition) {
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

    @Override
    public ResponseData proposedAssignments(String topic, List<Integer> brokerList) {
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

    @Override
    public ResponseEntity<byte[]> export() throws Exception {

        List<ClusterInfoDO> clusterInfoList = clusterInfoMapper.selectList(null);
        List<ClusterRoleRelationDO> clusterRoleRelationList = clusterRoleRelationMapper.selectList(null);
        List<SysRoleDO> sysRoleList = sysRoleMapper.selectList(null);
        List<SysUserDO> sysUserList = sysUserMapper.selectList(null);
        List<KafkaUserDO> kafkaUserList = kafkaUserMapper.selectList(null);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        ConsoleData consoleData = new ConsoleData();
        consoleData.setClusterInfoList(clusterInfoList);
        consoleData.setClusterRoleRelationList(clusterRoleRelationList);
        consoleData.setSysRoleList(sysRoleList);
        consoleData.setSysUserList(sysUserList);
        consoleData.setKafkaUserList(kafkaUserList);

        String jsonContent = objectMapper.writeValueAsString(consoleData);
        byte[] jsonBytes = jsonContent.getBytes();

        String filename = "kafka-console-ui-export.json";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(jsonBytes);
    }

    @Override
    public ResponseData importData(ConsoleData data) throws Exception {
        if (CollectionUtils.isNotEmpty(data.getClusterInfoList())) {
            Set<Long> idSet = clusterInfoMapper.selectList(new QueryWrapper<ClusterInfoDO>().select("id"))
                    .stream().map(ClusterInfoDO::getId).collect(Collectors.toSet());
            for (ClusterInfoDO infoDO : data.getClusterInfoList()) {
                if (!idSet.contains(infoDO.getId())) {
                    clusterInfoMapper.insert(infoDO);
                    log.info("Insert cluster info: {}", infoDO);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(data.getClusterRoleRelationList())) {
            Set<Long> idSet = clusterRoleRelationMapper.selectList(new QueryWrapper<ClusterRoleRelationDO>().select("id"))
                    .stream().map(ClusterRoleRelationDO::getId).collect(Collectors.toSet());
            for (ClusterRoleRelationDO relationDO : data.getClusterRoleRelationList()) {
                if (!idSet.contains(relationDO.getId())) {
                    clusterRoleRelationMapper.insert(relationDO);
                    log.info("Insert cluster role relation: {}", relationDO);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(data.getSysRoleList())) {
            Set<Long> idSet = sysRoleMapper.selectList(new QueryWrapper<SysRoleDO>().select("id"))
                    .stream().map(SysRoleDO::getId).collect(Collectors.toSet());
            for (SysRoleDO roleDO : data.getSysRoleList()) {
                if (!idSet.contains(roleDO.getId())) {
                    sysRoleMapper.insert(roleDO);
                    log.info("Insert sys role: {}", roleDO);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(data.getSysUserList())) {
            Set<Long> idSet = sysUserMapper.selectList(new QueryWrapper<SysUserDO>().select("id"))
                    .stream().map(SysUserDO::getId).collect(Collectors.toSet());
            for (SysUserDO userDO : data.getSysUserList()) {
                if (!idSet.contains(userDO.getId())) {
                    sysUserMapper.insert(userDO);
                    log.info("Insert sys user: {}", userDO);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(data.getKafkaUserList())) {
            Set<Long> idSet = kafkaUserMapper.selectList(new QueryWrapper<KafkaUserDO>().select("id"))
                    .stream().map(KafkaUserDO::getId).collect(Collectors.toSet());
            for (KafkaUserDO userDO : data.getKafkaUserList()) {
                if (!idSet.contains(userDO.getId())) {
                    kafkaUserMapper.insert(userDO);
                    log.info("Insert kafka user: {}", userDO);
                }
            }
        }
        return ResponseData.create().success();
    }
}
