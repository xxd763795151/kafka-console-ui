package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.beans.LoginResult;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import com.xuxd.kafka.console.beans.dos.SysUserDO;
import com.xuxd.kafka.console.beans.dto.LoginUserDTO;
import com.xuxd.kafka.console.cache.RolePermCache;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.service.AuthService;
import com.xuxd.kafka.console.utils.AuthUtil;
import com.xuxd.kafka.console.utils.UUIDStrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:01
 **/
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final AuthConfig authConfig;

    private final RolePermCache rolePermCache;

    public AuthServiceImpl(SysUserMapper userMapper,
                           SysRoleMapper roleMapper,
                           AuthConfig authConfig,
                           RolePermCache rolePermCache) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.authConfig = authConfig;
        this.rolePermCache = rolePermCache;
    }

    @Override
    public ResponseData login(LoginUserDTO userDTO) {
        QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        SysUserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO == null) {
            return ResponseData.create().failed("用户名/密码不正确");
        }
        String encrypt = UUIDStrUtil.generate(userDTO.getPassword(), userDO.getSalt());
        if (!userDO.getPassword().equals(encrypt)) {
            return ResponseData.create().failed("用户名/密码不正确");
        }
        Credentials credentials = new Credentials();
        credentials.setUsername(userDO.getUsername());
        credentials.setExpiration(System.currentTimeMillis() + authConfig.getExpireHours() * 3600 * 1000);
        String token = AuthUtil.generateToken(authConfig.getSecret(), credentials);
        LoginResult loginResult = new LoginResult();
        List<String> permissions = new ArrayList<>();
        String roleIds = userDO.getRoleIds();
        if (StringUtils.isNotEmpty(roleIds)) {
            List<String> roleIdList = Arrays.stream(roleIds.split(",")).map(String::trim).filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            roleIdList.forEach(roleId -> {
                Long rId = Long.valueOf(roleId);
                SysRoleDO roleDO = roleMapper.selectById(rId);
                String permissionIds = roleDO.getPermissionIds();
                if (StringUtils.isNotEmpty(permissionIds)) {
                    List<Long> permIds = Arrays.stream(permissionIds.split(",")).map(String::trim).
                            filter(StringUtils::isNotEmpty).map(Long::valueOf).collect(Collectors.toList());
                    permIds.forEach(id -> {
                        String permission = rolePermCache.getPermCache().get(id).getPermission();
                        if (StringUtils.isNotEmpty(permission)) {
                            permissions.add(permission);
                        } else {
                            log.error("角色：{}，权限id: {}，不存在", roleId, id);
                        }
                    });
                }
            });
        }
        loginResult.setToken(token);
        loginResult.setPermissions(permissions);
        return ResponseData.create().data(loginResult).success();
    }

}
