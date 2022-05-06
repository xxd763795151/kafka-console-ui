package com.xuxd.kafka.console.utils;

import com.xuxd.kafka.console.beans.ResponseData;

public class ResponseUtil {

    public static <T> ResponseData<T> success(T data) {
        return ResponseData.create().data(data);
    }

    public static ResponseData<String> error(String msg) {
        return ResponseData.create().failed(msg);
    }

}
