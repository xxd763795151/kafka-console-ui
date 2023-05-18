package com.xuxd.kafka.console.utils;

import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author: xuxd
 * @date: 2023/5/14 20:25
 **/
@Slf4j
public class MD5Util {

    public static MessageDigest getInstance() {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return md5;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String md5(String str) {
        MessageDigest digest = getInstance();
        if (digest == null) {
            return null;
        }
        return new String(digest.digest(str.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }
}
