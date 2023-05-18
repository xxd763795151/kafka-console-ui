package com.xuxd.kafka.console.cache;

import com.xuxd.kafka.console.beans.RolePermUpdateEvent;
import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @date: 2023/5/18 15:47
 **/
@DependsOn("dataInit")
@Slf4j
@Component
public class RolePermCache implements ApplicationListener<RolePermUpdateEvent>, SmartInitializingSingleton {

    private Map<Long, SysPermissionDO> permCache = new HashMap<>();

    private Map<Long, Set<String>> rolePermCache = new HashMap<>();

    private final SysPermissionMapper permissionMapper;

    private final SysRoleMapper roleMapper;

    public RolePermCache(SysPermissionMapper permissionMapper, SysRoleMapper roleMapper) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
    }

    @Override
    public void onApplicationEvent(RolePermUpdateEvent event) {
        log.info("更新角色权限信息：{}", event);
        if (event.isReload()) {
            this.loadPermCache();
        }
        refresh();
    }

    public Map<Long, SysPermissionDO> getPermCache() {
        return permCache;
    }

    public Map<Long, Set<String>> getRolePermCache() {
        return rolePermCache;
    }

    private void refresh() {
        List<SysRoleDO> roleDOS = roleMapper.selectList(null);
        Map<Long, Set<String>> tmp = new HashMap<>();
        for (SysRoleDO roleDO : roleDOS) {
            String permissionIds = roleDO.getPermissionIds();
            if (StringUtils.isEmpty(permissionIds)) {
                continue;
            }
            List<Long> list = Arrays.stream(permissionIds.split(",")).map(String::trim).filter(StringUtils::isNotEmpty).map(Long::valueOf).collect(Collectors.toList());
            Set<String> permSet = tmp.getOrDefault(roleDO.getId(), new HashSet<>());
            for (Long permId : list) {
                SysPermissionDO permissionDO = permCache.get(permId);
                if (permissionDO != null) {
                    permSet.add(permissionDO.getPermission());
                }
            }
            tmp.put(roleDO.getId(), permSet);
        }
        rolePermCache = tmp;
    }

    private void loadPermCache() {
        List<SysPermissionDO> roleDOS = permissionMapper.selectList(null);
        Map<Long, SysPermissionDO> map = roleDOS.stream().collect(Collectors.toMap(SysPermissionDO::getId, Function.identity(), (e1, e2) -> e1));
        permCache = map;
    }


    @Override
    public void afterSingletonsInstantiated() {
        this.loadPermCache();
        this.refresh();
    }
}
