package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.dos.SysUserDO;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/4/11 21:17
 **/
@Data
public class SysUserDTO {

    private String username;

    private String password;

    private String salt;

    private String roleIds;

    public SysUserDO toDO() {
        SysUserDO userDO = new SysUserDO();
        userDO.setUsername(this.username);
        userDO.setPassword(this.password);
        userDO.setSalt(this.salt);
        userDO.setRoleIds(this.roleIds);
        return userDO;
    }
}
