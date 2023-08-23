package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.dto.ClusterRoleRelationDTO;
import com.xuxd.kafka.console.service.ClusterRoleRelationService;
import org.springframework.web.bind.annotation.*;

/**
 * @author: xuxd
 * @since: 2023/8/23 22:01
 **/
@RestController
@RequestMapping("/cluster-role/relation")
public class ClusterRoleRelationController {

    private final ClusterRoleRelationService service;

    public ClusterRoleRelationController(ClusterRoleRelationService service) {
        this.service = service;
    }

    @Permission("user-manage:cluster-role")
    @GetMapping
    public Object select() {
        return service.select();
    }

    @ControllerLog("增加集群归属角色信息")
    @Permission("user-manage:cluster-role:add")
    @PostMapping
    public Object add(@RequestBody ClusterRoleRelationDTO dto) {
        return service.add(dto);
    }

    @ControllerLog("删除集群归属角色信息")
    @Permission("user-manage:cluster-role:delete")
    @DeleteMapping
    public Object delete(@RequestParam("id") Long id) {
        return service.delete(id);
    }
}
