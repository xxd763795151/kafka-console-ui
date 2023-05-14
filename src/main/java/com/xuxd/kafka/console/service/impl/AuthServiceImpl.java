package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.beans.LoginResult;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.SysUserDO;
import com.xuxd.kafka.console.beans.dto.LoginUserDTO;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.SysUserMapper;
import com.xuxd.kafka.console.service.AuthService;
import com.xuxd.kafka.console.utils.AuthUtil;
import com.xuxd.kafka.console.utils.UUIDStrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:01
 **/
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;

    private final AuthConfig authConfig;

    public AuthServiceImpl(SysUserMapper userMapper, AuthConfig authConfig) {
        this.userMapper = userMapper;
        this.authConfig = authConfig;
    }

    @Override
    public ResponseData login(LoginUserDTO userDTO) {
        QueryWrapper<SysUserDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userDTO.getUsername());
        SysUserDO userDO = userMapper.selectOne(queryWrapper);
        if (userDO == null) {
            return ResponseData.create().failed("用户名/密码不正确");
        }
        String encrypt = UUIDStrUtil.generate(userDTO.getPassword(), userDO.getSalt());
        if (!userDO.getPassword().equals(encrypt)) {
            return ResponseData.create().failed("用户名/密码不正确");
        }
        Credentials credentials = new Credentials();
        credentials.setUsername(userDO.getUsername());
        credentials.setExpiration(System.currentTimeMillis() + authConfig.getExpireHours() * 3600 * 1000);
        String token = AuthUtil.generateToken(authConfig.getSecret(), credentials);
        LoginResult loginResult = new LoginResult();
        loginResult.setToken(token);
        return ResponseData.create().data(loginResult).success();
    }
}
