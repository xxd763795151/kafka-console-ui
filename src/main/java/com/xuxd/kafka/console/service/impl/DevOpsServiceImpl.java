package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.xuxd.kafka.console.beans.KafkaConsoleException;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.DevOpsUserDO;
import com.xuxd.kafka.console.beans.dto.user.AddUserDTO;
import com.xuxd.kafka.console.beans.dto.user.ListUserDTO;
import com.xuxd.kafka.console.beans.dto.user.UpdateUserDTO;
import com.xuxd.kafka.console.beans.vo.DevOpsUserVO;
import com.xuxd.kafka.console.beans.vo.LoginVO;
import com.xuxd.kafka.console.boot.InitSuperDevOpsUser;
import com.xuxd.kafka.console.dao.DevOpsUserMapper;
import com.xuxd.kafka.console.service.DevOpsUserService;
import com.xuxd.kafka.console.utils.ConvertUtil;
import com.xuxd.kafka.console.utils.JwtUtils;
import com.xuxd.kafka.console.utils.Md5Utils;
import com.xuxd.kafka.console.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevOpsServiceImpl implements DevOpsUserService {

    private final DevOpsUserMapper devOpsUserMapper;

    @Override
    public ResponseData<Boolean> add(AddUserDTO addUserDTO) {
        QueryWrapper<DevOpsUserDO> queryWrapper = new QueryWrapper<DevOpsUserDO>();
        queryWrapper.eq("username", addUserDTO.getUsername());
        if (devOpsUserMapper.selectOne(queryWrapper) != null){
            throw new KafkaConsoleException("账号已存在");
        }

        addUserDTO.setPassword(Md5Utils.MD5(addUserDTO.getPassword()));
        int ret = devOpsUserMapper.insert(ConvertUtil.copy(addUserDTO, DevOpsUserDO.class));
        return ResponseUtil.success(ret > 0);
    }

    @Override
    public ResponseData<Boolean> update(UpdateUserDTO updateUserDTO) {
        UpdateWrapper<DevOpsUserDO> updateWrapper = new UpdateWrapper<>();
        if (updateUserDTO.getRole() != null){
            updateWrapper.set("role", updateUserDTO.getRole());
        }
        if (StringUtils.isNotBlank(updateUserDTO.getPassword())){
            updateWrapper.set("password", Md5Utils.MD5(updateUserDTO.getPassword()));
        }
        updateWrapper.eq("username", updateUserDTO.getUsername());
        int ret = devOpsUserMapper.update(null, updateWrapper);
        return ResponseUtil.success(ret > 0);
    }

    @Override
    public ResponseData<Boolean> delete(Long id) {
        int ret = devOpsUserMapper.deleteById(id);
        return ResponseUtil.success(ret > 0);
    }

    @Override
    public ResponseData<List<DevOpsUserVO>> list(ListUserDTO listUserDTO) {
        QueryWrapper<DevOpsUserDO> queryWrapper = new QueryWrapper<DevOpsUserDO>();
        if (listUserDTO.getId() != null){
            queryWrapper.eq("id", listUserDTO.getId());
        }
        if (StringUtils.isNotBlank(listUserDTO.getUsername())){
            queryWrapper.eq("username", listUserDTO.getUsername());
        }
        queryWrapper.ne("username", InitSuperDevOpsUser.SUPER_USERNAME);
        List<DevOpsUserDO> userDOS = devOpsUserMapper.selectList(queryWrapper);
        return ResponseUtil.success(ConvertUtil.copyList(userDOS, DevOpsUserVO.class));
    }

    @Override
    public ResponseData<DevOpsUserVO> detail(String username) {
        QueryWrapper<DevOpsUserDO> queryWrapper = new QueryWrapper<DevOpsUserDO>();
        queryWrapper.eq("username", username);
        DevOpsUserDO userDO = devOpsUserMapper.selectOne(queryWrapper);
        return ResponseUtil.success(ConvertUtil.copy(userDO, DevOpsUserVO.class));
    }

    @Override
    public ResponseData<LoginVO> login(String username, String password) {
        QueryWrapper<DevOpsUserDO> queryWrapper = new QueryWrapper<DevOpsUserDO>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("password", Md5Utils.MD5(password));
        DevOpsUserDO userDO = devOpsUserMapper.selectOne(queryWrapper);
        if (userDO == null){
            throw new KafkaConsoleException("用户名或密码错误");
        }
        LoginVO loginVO = LoginVO.builder().role(userDO.getRole()).token(JwtUtils.sign(username)).build();
        return ResponseUtil.success(loginVO);
    }
}
