package com.xuxd.kafka.console.utils;

import com.google.gson.Gson;
import com.xuxd.kafka.console.beans.Credentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Base64Utils;

import java.nio.charset.StandardCharsets;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:34
 **/
@Slf4j
public class AuthUtil {

    private static Gson gson = GsonUtil.INSTANCE.get();

    public static String generateToken(String secret, Credentials info) {
        String json = gson.toJson(info);
        String str = json + secret;
        String signature = MD5Util.md5(str);
        return Base64Utils.encodeToString(json.getBytes(StandardCharsets.UTF_8)) + "." +
                Base64Utils.encodeToString(signature.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isToken(String token) {
        return token.split("\\.").length == 2;
    }

    public static Credentials parseToken(String secret, String token) {
        if (!isToken(token)) {
            return Credentials.INVALID;
        }
        String[] arr = token.split("\\.");
        String infoStr = new String(Base64Utils.decodeFromString(arr[0]), StandardCharsets.UTF_8);
        String signature = new String(Base64Utils.decodeFromString(arr[1]), StandardCharsets.UTF_8);

        String encrypt = MD5Util.md5(infoStr + secret);
        if (!encrypt.equals(signature)) {
            return Credentials.INVALID;
        }
        try {
            Credentials credentials = gson.fromJson(infoStr, Credentials.class);
            if (credentials.getExpiration() < System.currentTimeMillis()) {
                return Credentials.INVALID;
            }
            return credentials;
        } catch (Exception e) {
            log.error("解析token失败: {}", token, e);
            return Credentials.INVALID;
        }
    }
}
