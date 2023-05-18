package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:17
 **/
@Data
public class SysPermissionDTO {

    private String name;

    /**
     * 权限类型: 0：菜单，1：按钮
     */
    private Integer type;

    private Long parentId;

    private String permission;

    public SysPermissionDO toSysPermissionDO() {
        SysPermissionDO permissionDO = new SysPermissionDO();
        permissionDO.setName(this.name);
        permissionDO.setType(this.type);
        permissionDO.setParentId(this.parentId);
        permissionDO.setPermission(this.permission);
        return permissionDO;
    }
}
