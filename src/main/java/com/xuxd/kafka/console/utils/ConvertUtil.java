package com.xuxd.kafka.console.utils;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.beans.BeanCopier;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.ClassUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 16:21:43
 **/
@Slf4j
public class ConvertUtil {

    private static ThreadLocal<ObjenesisStd> objenesisStdThreadLocal = ThreadLocal.withInitial(ObjenesisStd::new);
    private static ConcurrentHashMap<Class<?>, ConcurrentHashMap<Class<?>, BeanCopier>> cache = new ConcurrentHashMap<>();

    public static <T> T copy(Object source, Class<T> target) {
        return copy(source, objenesisStdThreadLocal.get().newInstance(target));
    }

    public static <T> T copy(Object source, T target) {
        if (null == source) {
            return null;
        }
        BeanCopier beanCopier = getCacheBeanCopier(source.getClass(), target.getClass());
        beanCopier.copy(source, target, null);
        return target;
    }

    public static <T> List<T> copyList(List<?> sources, Class<T> target) {
        if (sources.isEmpty()) {
            return Collections.emptyList();
        }

        ArrayList<T> list = new ArrayList<>(sources.size());
        ObjenesisStd objenesisStd = objenesisStdThreadLocal.get();
        for (Object source : sources) {
            if (source == null) {
                break;
            }
            T newInstance = objenesisStd.newInstance(target);
            BeanCopier beanCopier = getCacheBeanCopier(source.getClass(), target);
            beanCopier.copy(source, newInstance, null);
            list.add(newInstance);
        }
        return list;
    }

    private static <S, T> BeanCopier getCacheBeanCopier(Class<S> source, Class<T> target) {
        ConcurrentHashMap<Class<?>, BeanCopier> copierConcurrentHashMap =
                cache.computeIfAbsent(source, aClass -> new ConcurrentHashMap<>(16));
        return copierConcurrentHashMap.computeIfAbsent(target, aClass -> BeanCopier.create(source, target, false));
    }

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
