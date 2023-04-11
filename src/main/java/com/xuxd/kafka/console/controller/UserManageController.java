package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;
import com.xuxd.kafka.console.service.UserManageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:34
 **/
@RestController
@RequestMapping("/sys/user/manage")
public class UserManageController {

    private final UserManageService userManageService;

    public UserManageController(UserManageService userManageService) {
        this.userManageService = userManageService;
    }

    @ControllerLog("新增用户")
    @PostMapping("/user")
    public Object addUser(@RequestBody SysUserDTO userDTO) {
        return userManageService.addUser(userDTO);
    }

    @ControllerLog("新增角色")
    @PostMapping("/role")
    public Object addRole(@RequestBody SysRoleDTO roleDTO) {
        return userManageService.addRole(roleDTO);
    }

    @ControllerLog("新增权限")
    @PostMapping("/permission")
    public Object addPermission(@RequestBody SysPermissionDTO permissionDTO) {
        return userManageService.addPermission(permissionDTO);
    }
}
