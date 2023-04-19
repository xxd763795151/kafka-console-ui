package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/4/19 21:12
 **/
@Data
public class SysRoleVO {

    private String roleName;

    private String description;

    private String permissionIds;

    public static SysRoleVO from(SysRoleDO roleDO) {
        SysRoleVO roleVO = new SysRoleVO();
        roleVO.setRoleName(roleDO.getRoleName());
        roleVO.setDescription(roleDO.getDescription());
        roleVO.setPermissionIds(roleDO.getPermissionIds());
        return roleVO;
    }
}
