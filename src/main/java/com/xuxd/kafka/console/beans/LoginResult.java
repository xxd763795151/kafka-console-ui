package com.xuxd.kafka.console.beans;

import lombok.Data;

import java.util.List;

/**
 * @author: xuxd
 * @date: 2023/5/14 20:44
 **/
@Data
public class LoginResult {

    private String token;

    private List<String> permissions;
}
