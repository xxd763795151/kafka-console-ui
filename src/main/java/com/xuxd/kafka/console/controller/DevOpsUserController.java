package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.annotation.RequiredAuthorize;
import com.xuxd.kafka.console.beans.dto.user.*;
import com.xuxd.kafka.console.beans.vo.DevOpsUserVO;
import com.xuxd.kafka.console.beans.vo.LoginVO;
import com.xuxd.kafka.console.service.DevOpsUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理
 * @author dongyinuo
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/devops/user")
public class DevOpsUserController {

    private final DevOpsUserService devOpsUserService;

    @PostMapping("add")
    @RequiredAuthorize
    public ResponseData<Boolean> add(@RequestBody AddUserDTO addUserDTO){
        return devOpsUserService.add(addUserDTO);
    }

    @PostMapping("update")
    @RequiredAuthorize
    public ResponseData<Boolean> update(@RequestBody UpdateUserDTO updateUserDTO){
        return devOpsUserService.update(updateUserDTO);
    }

    @DeleteMapping
    @RequiredAuthorize
    public ResponseData<Boolean> delete(@RequestParam Long id){
        return devOpsUserService.delete(id);
    }

    @GetMapping("list")
    @RequiredAuthorize
    public ResponseData<List<DevOpsUserVO>> list(@ModelAttribute ListUserDTO listUserDTO){
        return devOpsUserService.list(listUserDTO);
    }

    @PostMapping("login")
    public ResponseData<LoginVO> login(@RequestBody LoginDTO loginDTO){
        return devOpsUserService.login(loginDTO.getUsername(), loginDTO.getPassword());
    }
}
