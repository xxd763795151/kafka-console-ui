package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.SysPermissionDTO;
import com.xuxd.kafka.console.beans.dto.SysRoleDTO;
import com.xuxd.kafka.console.beans.dto.SysUserDTO;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.service.UserManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:24
 **/
@Slf4j
@Service
public class UserManageServiceImpl implements UserManageService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysPermissionMapper permissionMapper;

    public UserManageServiceImpl(ObjectProvider<SysUserMapper> userMapper,
                                 ObjectProvider<SysRoleMapper> roleMapper,
                                 ObjectProvider<SysPermissionMapper> permissionMapper) {
        this.userMapper = userMapper.getIfAvailable();
        this.roleMapper = roleMapper.getIfAvailable();
        this.permissionMapper = permissionMapper.getIfAvailable();
    }

    @Override
    public ResponseData addPermission(SysPermissionDTO permissionDTO) {
        permissionMapper.insert(permissionDTO.toSysPermissionDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData addRole(SysRoleDTO roleDTO) {
        roleMapper.insert(roleDTO.toDO());
        return ResponseData.create().success();
    }

    @Override
    public ResponseData addUser(SysUserDTO userDTO) {
        userMapper.insert(userDTO.toDO());
        return ResponseData.create().success();
    }
}
