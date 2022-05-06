package com.xuxd.kafka.console.utils;

import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

public class Md5Utils {

    public static String MD5(String s) {
        return DigestUtils.md5DigestAsHex(s.getBytes(StandardCharsets.UTF_8));
    }
}
