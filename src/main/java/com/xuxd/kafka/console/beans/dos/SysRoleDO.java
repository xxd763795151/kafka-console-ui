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
@TableName("t_sys_role")
public class SysRoleDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleName;

    private String description;

    private String permissionIds;
}
