package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.LoginUserDTO;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:00
 **/
public interface AuthService {

    ResponseData login(LoginUserDTO userDTO);

    boolean ownDataAuthority();
}
