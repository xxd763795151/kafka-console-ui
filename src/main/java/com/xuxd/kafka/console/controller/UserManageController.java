package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;
import com.xuxd.kafka.console.service.UserManageService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

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

    @ControllerLog("新增/更新用户")
    @PostMapping("/user")
    public Object addOrUpdateUser(@RequestBody SysUserDTO userDTO) {
        return userManageService.addOrUpdateUser(userDTO);
    }

    @ControllerLog("新增/更新角色")
    @PostMapping("/role")
    public Object addOrUpdateRole(@RequestBody SysRoleDTO roleDTO) {
        return userManageService.addOrUdpateRole(roleDTO);
    }

    @ControllerLog("新增权限")
    @PostMapping("/permission")
    public Object addPermission(@RequestBody SysPermissionDTO permissionDTO) {
        return userManageService.addPermission(permissionDTO);
    }

    @ControllerLog("更新角色")
    @PutMapping("/role")
    public Object updateRole(@RequestBody SysRoleDTO roleDTO) {
        return userManageService.updateRole(roleDTO);
    }

    @GetMapping("/role")
    public Object selectRole() {
        return userManageService.selectRole();
    }

    @GetMapping("/permission")
    public Object selectPermission() {
        return userManageService.selectPermission();
    }

    @GetMapping("/user")
    public Object selectUser() {
        return userManageService.selectUser();
    }

    @ControllerLog("删除角色")
    @DeleteMapping("/role")
    public Object deleteRole(@RequestParam Long id) {
        return userManageService.deleteRole(id);
    }

    @ControllerLog("删除用户")
    @DeleteMapping("/user")
    public Object deleteUser(@RequestParam Long id) {
        return userManageService.deleteUser(id);
    }

    @ControllerLog("更新密码")
    @PostMapping("/user/password")
    public Object updatePassword(@RequestBody SysUserDTO userDTO, HttpServletRequest request) {
        Credentials credentials = (Credentials)request.getAttribute("credentials");
        if (credentials != null && !credentials.isInvalid()) {
            userDTO.setUsername(credentials.getUsername());
        }
        return userManageService.updatePassword(userDTO);
    }
}
