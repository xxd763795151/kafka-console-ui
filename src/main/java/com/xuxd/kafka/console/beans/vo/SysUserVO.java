package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.SysUserDO;
import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/5/6 13:06
 **/
@Data
public class SysUserVO {

    private Long id;

    private String username;

    private String password;

    private String roleIds;

    public static SysUserVO from(SysUserDO userDO) {
        SysUserVO userVO = new SysUserVO();
        userVO.setId(userDO.getId());
        userVO.setUsername(userDO.getUsername());
        userVO.setRoleIds(userDO.getRoleIds());
        userVO.setPassword(userDO.getPassword());
        return userVO;
    }
}
