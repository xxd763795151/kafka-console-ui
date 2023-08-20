package com.xuxd.kafka.console.aspect;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.beans.dos.SysUserDO;
import com.xuxd.kafka.console.cache.RolePermCache;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.SysPermissionMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.exception.UnAuthorizedException;
import com.xuxd.kafka.console.filter.CredentialsContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @date: 2023/5/17 22:32
 **/
@Slf4j
@Order(1)
@Aspect
@Component
public class PermissionAspect {


    private Map<String, Set<String>> permMap = new HashMap<>();

    private final AuthConfig authConfig;

    private final SysUserMapper userMapper;

    private final SysRoleMapper roleMapper;

    private final SysPermissionMapper permissionMapper;

    private final RolePermCache rolePermCache;

    public PermissionAspect(AuthConfig authConfig,
                            SysUserMapper userMapper,
                            SysRoleMapper roleMapper,
                            SysPermissionMapper permissionMapper,
                            RolePermCache rolePermCache) {
        this.authConfig = authConfig;
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermCache = rolePermCache;
    }

    @Pointcut("@annotation(com.xuxd.kafka.console.aspect.annotation.Permission)")
    private void pointcut() {

    }

    @Before(value = "pointcut()")
    public void before(JoinPoint joinPoint) {
        if (!authConfig.isEnable()) {
            return;
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Permission permission = method.getAnnotation(Permission.class);
        if (permission == null) {
            return;
        }
        String[] value = permission.value();
        if (value == null || value.length == 0) {
            return;
        }
        String name = method.getName() + "@" + method.hashCode();

        Map<String, Set<String>> pm = checkPermMap(name, value);

        Set<String> allowPermSet = pm.get(name);
        if (allowPermSet == null) {
            log.error("解析权限出现意外啦!!!");
            return;
        }

        Credentials credentials = CredentialsContext.get();
        if (credentials == null || credentials.isInvalid()) {
            throw new UnAuthorizedException("credentials is invalid");
        }
        QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", credentials.getUsername());
        SysUserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO == null) {
            throw new UnAuthorizedException(credentials.getUsername() + ":" + allowPermSet);
        }

        boolean unauthorized = true;
        boolean notFoundHideProperty = true;
        String roleIds = userDO.getRoleIds();
        List<Long> roleIdList = Arrays.stream(roleIds.split(",")).map(String::trim).filter(StringUtils::isNotEmpty).map(Long::valueOf).collect(Collectors.toList());
        for (Long roleId : roleIdList) {
            Set<String> permSet = rolePermCache.getRolePermCache().getOrDefault(roleId, Collections.emptySet());
            for (String p : allowPermSet) {
                if (permSet.contains(p)) {
                    unauthorized = false;
                }
            }
            if (permSet.contains(authConfig.getHideClusterPropertyPerm())) {
                notFoundHideProperty = false;
            }
        }
        if (unauthorized) {
            throw new UnAuthorizedException(credentials.getUsername() + ":" + allowPermSet);
        }
        if (authConfig.isHideClusterProperty() && notFoundHideProperty) {
            credentials.setHideClusterProperty(true);
        }
    }

    private Map<String, Set<String>> checkPermMap(String methodName, String[] value) {
        if (!permMap.containsKey(methodName)) {
            Map<String, Set<String>> map = new HashMap<>(permMap);
            map.put(methodName, new HashSet<>(Arrays.asList(value)));
            permMap = map;
            return map;
        }
        return permMap;
    }

}
