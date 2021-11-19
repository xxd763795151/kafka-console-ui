package com.xuxd.kafka.console.utils;

import com.google.gson.Gson;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-19 17:01:01
 **/
public enum GsonUtil {
    INSTANCE;

    private Gson gson = new Gson();

    public Gson get() {
        return gson;
    }
}
