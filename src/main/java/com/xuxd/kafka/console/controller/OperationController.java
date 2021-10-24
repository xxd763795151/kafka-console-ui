package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.dto.SyncDataDTO;
import com.xuxd.kafka.console.service.OperationService;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
