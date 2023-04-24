package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;

/**
 * 登录用户权限管理.
 *
 * @author: xuxd
 * @date: 2023/4/11 21:24
 **/
public interface UserManageService {

    /**
     * 增加权限
     */
    ResponseData addPermission(SysPermissionDTO permissionDTO);

    ResponseData addRole(SysRoleDTO roleDTO);

    ResponseData addUser(SysUserDTO userDTO);

    ResponseData selectRole();

    ResponseData selectPermission();

    ResponseData updateUser(SysUserDTO userDTO);

    ResponseData updateRole(SysRoleDTO roleDTO);
}
