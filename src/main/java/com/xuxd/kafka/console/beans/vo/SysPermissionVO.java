package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.SysPermissionDO;
import lombok.Data;

import java.util.List;

/**
 * @author: xuxd
 * @date: 2023/4/17 21:18
 **/
@Data
public class SysPermissionVO {

    private Long id;

    private String name;

    /**
     * 权限类型: 0：菜单，1：按钮
     */
    private Integer type;

    private Long parentId;

    private String permission;

    private Long key;

    private List<SysPermissionVO> children;

    public static SysPermissionVO from(SysPermissionDO permissionDO) {
        SysPermissionVO permissionVO = new SysPermissionVO();

        permissionVO.setPermission(permissionDO.getPermission());
        permissionVO.setType(permissionDO.getType());
        permissionVO.setName(permissionDO.getName());
        permissionVO.setParentId(permissionDO.getParentId());
        permissionVO.setKey(permissionDO.getId());
        permissionVO.setId(permissionDO.getId());
        return permissionVO;
    }
}
