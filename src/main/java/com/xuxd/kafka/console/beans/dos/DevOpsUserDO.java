package com.xuxd.kafka.console.beans.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xuxd.kafka.console.beans.enums.Role;
import lombok.Data;

import java.util.Date;

@Data
@TableName("t_devops_user")
public class DevOpsUserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private Role role;

    private boolean delete;

    private Date createTime;

    private Date updateTime;
}
