package com.xuxd.kafka.console.beans.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xuxd.kafka.console.beans.enums.Role;
import lombok.Data;

import java.util.Date;

@Data
public class DevOpsUserVO {
    private Long id;
    private String username;
    private Role role;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createTime;
}
