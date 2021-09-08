package com.xuxd.kafka.console.utils;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
        return res;
    }
}
