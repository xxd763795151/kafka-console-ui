package com.xuxd.kafka.console.boot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.dos.DevOpsUserDO;
import com.xuxd.kafka.console.beans.enums.Role;
import com.xuxd.kafka.console.dao.DevOpsUserMapper;
import com.xuxd.kafka.console.utils.Md5Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class InitSuperDevOpsUser implements SmartInitializingSingleton {

    private final DevOpsUserMapper devOpsUserMapper;
    public final static String SUPER_USERNAME = "admin";

    @Value("${devops.password:kafka-console-ui521}")
    private String password;

    @Override
    public void afterSingletonsInstantiated() {
        QueryWrapper<DevOpsUserDO> userDOQueryWrapper = new QueryWrapper<>();
        userDOQueryWrapper.eq("username", SUPER_USERNAME);
        DevOpsUserDO userDO = devOpsUserMapper.selectOne(userDOQueryWrapper);
        if (userDO == null){
            DevOpsUserDO devOpsUserDO = new DevOpsUserDO();
            devOpsUserDO.setUsername(SUPER_USERNAME);
            devOpsUserDO.setPassword(Md5Utils.MD5(password));
            devOpsUserDO.setRole(Role.manager);
            devOpsUserMapper.insert(devOpsUserDO);
        } else {
            userDO.setPassword(Md5Utils.MD5(password));
            devOpsUserMapper.updateById(userDO);
        }
        log.info("init super devops user done, username = {}", SUPER_USERNAME);
    }
}
