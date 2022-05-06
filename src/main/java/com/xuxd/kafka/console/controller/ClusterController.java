package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.dto.ClusterInfoDTO;
import com.xuxd.kafka.console.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/info")
    public Object getClusterInfoList() {
        return clusterService.getClusterInfoList();
    }

    @PostMapping("/info")
    public Object addClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.addClusterInfo(dto.to());
    }

    @DeleteMapping("/info")
    public Object deleteClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.deleteClusterInfo(dto.getId());
    }

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
