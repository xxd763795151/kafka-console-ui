package com.xuxd.kafka.console.utils;

import java.util.HashMap;
import java.util.Map;

public class ContextUtil {

    public static final String USERNAME = "username" ;

    private static ThreadLocal<Map<String, Object>> context = ThreadLocal.withInitial(() -> new HashMap<>());

    public static void set(String key, Object value){
        context.get().put(key, value);
    }

    public static String get(String key){
        return (String) context.get().get(key);
    }

    public static void clear(){
        context.remove();
    }
}
