package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:17
 **/
@Data
public class SysRoleDTO {

    private Long id;

    private String roleName;

    private String description;

    private String permissionIds;

    public SysRoleDO toDO() {
        SysRoleDO roleDO = new SysRoleDO();
        roleDO.setId(this.id);
        roleDO.setRoleName(this.roleName);
        roleDO.setDescription(this.description);
        roleDO.setPermissionIds(this.permissionIds);
        return roleDO;
    }
}
