package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.dto.ClusterInfoDTO;
import com.xuxd.kafka.console.service.ClusterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/list")
    public Object getClusterInfoList() {
        return clusterService.getClusterInfoList();
    }

    @PostMapping
    public Object addClusterInfo(@RequestBody ClusterInfoDTO dto) {
        return clusterService.addClusterInfo(dto.to());
    }
}
