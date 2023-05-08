package com.xuxd.kafka.console.utils;

import java.util.Random;

/**
 * @author: xuxd
 * @date: 2023/5/8 9:19
 **/
public class RandomStringUtil {

    private final static String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public static String random6Str() {
        return generateRandomString(6);
    }

    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(ALLOWED_CHARS.length());
            sb.append(ALLOWED_CHARS.charAt(index));
        }
        return sb.toString();
    }
}
