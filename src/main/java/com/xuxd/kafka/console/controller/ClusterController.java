package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.dto.ClusterInfoDTO;
import com.xuxd.kafka.console.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:26:11
 **/
@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    @GetMapping
    public Object getClusterInfo() {
        return clusterService.getClusterInfo();
    }

    @Permission("op:cluster-switch")
    @GetMapping("/info")
    public Object getClusterInfoList() {
        return clusterService.getClusterInfoList();
    }

    @ControllerLog("增加集群信息")
    @Permission("op:cluster-switch:add")
    @PostMapping("/info")
    public Object addClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.addClusterInfo(dto.to());
    }

    @ControllerLog("删除集群信息")
    @Permission("op:cluster-switch:del")
    @DeleteMapping("/info")
    public Object deleteClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.deleteClusterInfo(dto.getId());
    }

    @ControllerLog("编辑集群信息")
    @Permission("op:cluster-switch:edit")
    @PutMapping("/info")
    public Object updateClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.updateClusterInfo(dto.to());
    }

    @GetMapping("/info/peek")
    public Object peekClusterInfo() {
        return clusterService.peekClusterInfo();
    }

    @GetMapping("/info/api/version")
    public Object getBrokerApiVersionInfo() {
        return clusterService.getBrokerApiVersionInfo();
    }
}
