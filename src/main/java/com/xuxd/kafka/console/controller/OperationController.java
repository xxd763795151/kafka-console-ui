package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.TopicPartition;
import com.xuxd.kafka.console.beans.dto.BrokerThrottleDTO;
import com.xuxd.kafka.console.beans.dto.ProposedAssignmentDTO;
import com.xuxd.kafka.console.beans.dto.ReplicationDTO;
import com.xuxd.kafka.console.beans.dto.SyncDataDTO;
import com.xuxd.kafka.console.service.OperationService;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:13:28
 **/
@RestController
@RequestMapping("/op")
public class OperationController {

    @Autowired
    private OperationService operationService;

    @PostMapping("/sync/consumer/offset")
    public Object syncConsumerOffset(@RequestBody SyncDataDTO dto) {
        dto.getProperties().put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, dto.getAddress());
        return operationService.syncConsumerOffset(dto.getGroupId(), dto.getTopic(), dto.getProperties());
    }

    @PostMapping("/sync/min/offset/alignment")
    public Object minOffsetAlignment(@RequestBody SyncDataDTO dto) {
        dto.getProperties().put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, dto.getAddress());
        return operationService.minOffsetAlignment(dto.getGroupId(), dto.getTopic(), dto.getProperties());
    }

    @GetMapping("/sync/alignment/list")
    public Object getAlignmentList() {
        return operationService.getAlignmentList();
    }

    @DeleteMapping("/sync/alignment")
    public Object deleteAlignment(@RequestParam("id") Long id) {
        return operationService.deleteAlignmentById(id);
    }

    @Permission({"topic:partition-detail:preferred", "op:replication-preferred"})
    @PostMapping("/replication/preferred")
    public Object electPreferredLeader(@RequestBody ReplicationDTO dto) {
        return operationService.electPreferredLeader(dto.getTopic(), dto.getPartition());
    }

    @Permission("op:config-throttle")
    @PostMapping("/broker/throttle")
    public Object configThrottle(@RequestBody BrokerThrottleDTO dto) {
        return operationService.configThrottle(dto.getBrokerList(), dto.getUnit().toKb(dto.getThrottle()));
    }

    @Permission("op:remove-throttle")
    @DeleteMapping("/broker/throttle")
    public Object removeThrottle(@RequestBody BrokerThrottleDTO dto) {
        return operationService.removeThrottle(dto.getBrokerList());
    }

    @Permission("op:replication-update-detail")
    @GetMapping("/replication/reassignments")
    public Object currentReassignments() {
        return operationService.currentReassignments();
    }

    @Permission("op:replication-update-detail:cancel")
    @DeleteMapping("/replication/reassignments")
    public Object cancelReassignment(@RequestBody TopicPartition partition) {
        return operationService.cancelReassignment(new org.apache.kafka.common.TopicPartition(partition.getTopic(), partition.getPartition()));
    }

    @PostMapping("/replication/reassignments/proposed")
    public Object proposedAssignments(@RequestBody ProposedAssignmentDTO dto) {
        return operationService.proposedAssignments(dto.getTopic(), dto.getBrokers());
    }
}
