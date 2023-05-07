package com.xuxd.kafka.console.utils;

import java.util.UUID;

/**
 * @author: xuxd
 * @date: 2023/5/6 13:30
 **/
public class UUIDStrUtil {

    public static String random() {
        return UUID.randomUUID().toString();
    }

    public static String generate(String ... strs) {
        StringBuilder sb = new StringBuilder();
        for (String str : strs) {
            sb.append(str);
        }
        return UUID.nameUUIDFromBytes(sb.toString().getBytes()).toString();
    }
}
