package com.xuxd.kafka.console.utils;

import com.google.common.base.Preconditions;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ClassUtils;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 16:21:43
 **/
@Slf4j
public class ConvertUtil {

    public static Map<String, Object> toMap(Object src) {
        Preconditions.checkNotNull(src);
        Map<String, Object> res = new HashMap<>();
        for (Class<?> clz = src.getClass(); clz != Object.class; clz = clz.getSuperclass()) {
            if (ClassUtils.isCglibProxyClass(clz)) {
                continue;
            }
            Arrays.stream(clz.getDeclaredFields()).forEach(f -> {

                try {
                    boolean accessible = f.isAccessible();
                    f.setAccessible(true);
                    res.put(f.getName(), f.get(src));
                    f.setAccessible(accessible);
                } catch (IllegalAccessException ignore) {
                    log.error("filed: " + f.getName(), ignore);
                }
            });
        }
        Iterator<Map.Entry<String, Object>> iterator = res.entrySet().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue() == null) {
                iterator.remove();
            }
        }
        return res;
    }

    public static String toJsonString(Object src) {
        return GsonUtil.INSTANCE.get().toJson(src);
    }

    public static Properties toProperties(String jsonStr) {
        return GsonUtil.INSTANCE.get().fromJson(jsonStr, Properties.class);
    }

    public static String jsonStr2PropertiesStr(String jsonStr) {
        StringBuilder sb = new StringBuilder();
        Map<String, Object> map = GsonUtil.INSTANCE.get().fromJson(jsonStr, Map.class);
        map.keySet().forEach(k -> {
            sb.append(k).append("=").append(map.get(k).toString()).append(System.lineSeparator());
        });

        return sb.toString();
    }

    public static List<String> jsonStr2List(String jsonStr) {
        List<String> res = new LinkedList<>();
        Map<String, Object> map = GsonUtil.INSTANCE.get().fromJson(jsonStr, Map.class);
        map.forEach((k, v) -> {
            res.add(k + "=" + v);
        });

        return res;
    }

    public static String propertiesStr2JsonStr(String propertiesStr) {
        String res = "{}";
        try (ByteArrayInputStream baos = new ByteArrayInputStream(propertiesStr.getBytes())) {
            Properties properties = new Properties();
            properties.load(baos);
            res = toJsonString(properties);
        } catch (IOException e) {
            log.error("propertiesStr2JsonStr error.", e);
        }
        return res;
    }
}
