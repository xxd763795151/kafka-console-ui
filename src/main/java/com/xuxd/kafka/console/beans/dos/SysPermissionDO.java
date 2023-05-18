package com.xuxd.kafka.console.beans.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:17
 **/
@Data
@TableName("t_sys_permission")
public class SysPermissionDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /**
     * 权限类型: 0：菜单，1：按钮
     */
    private Integer type;

    private Long parentId;

    private String permission;
}
