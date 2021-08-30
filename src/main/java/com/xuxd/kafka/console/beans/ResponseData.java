package com.xuxd.kafka.console.beans;

import lombok.Getter;
import lombok.Setter;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 19:29:11
 **/
public class ResponseData<T> {

    public static final int SUCCESS_CODE = 0, FAILED_CODE = -9999;

    public static final String SUCCESS_MSG = "success", FAILED_MSG = "failed";

    @Getter
    @Setter
    private int code = SUCCESS_CODE;

    @Getter
    @Setter
    private String msg = SUCCESS_MSG;

    @Getter
    @Setter
    private T data;

    public static ResponseData create() {
        return new ResponseData();
    }

    public static <T> ResponseData create(Class<T> cls) {
        return new ResponseData<T>();
    }

    public ResponseData<T> data(T t) {
        this.data = t;
        return this;
    }

    public ResponseData<T> success() {
        this.code = SUCCESS_CODE;
        this.msg = SUCCESS_MSG;
        return this;
    }

    public ResponseData<T> success(String msg) {
        this.code = SUCCESS_CODE;
        this.msg = msg;
        return this;
    }

    public ResponseData<T> failed() {
        this.code = FAILED_CODE;
        this.msg = FAILED_MSG;
        return this;
    }

    public ResponseData<T> failed(String msg) {
        this.code = FAILED_CODE;
        this.msg = msg;
        return this;
    }

    @Override public String toString() {
        return "ResponseData{" +
            "code=" + code +
            ", msg='" + msg + '\'' +
            ", data=" + data +
            '}';
    }
}
