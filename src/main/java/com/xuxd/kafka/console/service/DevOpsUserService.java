package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.user.AddUserDTO;
import com.xuxd.kafka.console.beans.dto.user.ListUserDTO;
import com.xuxd.kafka.console.beans.dto.user.UpdateUserDTO;
import com.xuxd.kafka.console.beans.vo.DevOpsUserVO;
import com.xuxd.kafka.console.beans.vo.LoginVO;

import java.util.List;

public interface DevOpsUserService {

    ResponseData<Boolean> add(AddUserDTO addUserDTO);

    ResponseData<Boolean> update(UpdateUserDTO updateUserDTO);

    ResponseData<Boolean> delete(Long id);

    ResponseData<List<DevOpsUserVO>> list(ListUserDTO listUserDTO);

    ResponseData<DevOpsUserVO> detail(String username);

    ResponseData<LoginVO> login(String username, String password);

}
