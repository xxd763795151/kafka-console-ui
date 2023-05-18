package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.LoginUserDTO;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.service.AuthService;
import org.springframework.web.bind.annotation.*;

/**
 * @author: xuxd
 * @date: 2023/5/11 18:54
 **/
@RestController
@RequestMapping("/auth")
public class AuthController {


    private final AuthConfig authConfig;

    private final AuthService authService;

    public AuthController(AuthConfig authConfig, AuthService authService) {
        this.authConfig = authConfig;
        this.authService = authService;
    }

    @GetMapping("/enable")
    public boolean enable() {
        return authConfig.isEnable();
    }

    @PostMapping("/login")
    public ResponseData login(@RequestBody LoginUserDTO userDTO) {
        return authService.login(userDTO);
    }
}
