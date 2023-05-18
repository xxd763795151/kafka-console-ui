package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @date: 2023/4/19 21:12
 **/
@Data
public class SysRoleVO {

    private Long id;

    private String roleName;

    private String description;

    private List<Long> permissionIds;

    public static SysRoleVO from(SysRoleDO roleDO) {
        SysRoleVO roleVO = new SysRoleVO();
        roleVO.setId(roleDO.getId());
        roleVO.setRoleName(roleDO.getRoleName());
        roleVO.setDescription(roleDO.getDescription());
        if (StringUtils.isNotEmpty(roleDO.getPermissionIds())) {
            List<Long> list = Arrays.stream(roleDO.getPermissionIds().split(",")).
                    filter(StringUtils::isNotEmpty).map(e -> Long.valueOf(e.trim())).collect(Collectors.toList());
            roleVO.setPermissionIds(list);
        }
        return roleVO;
    }
}
