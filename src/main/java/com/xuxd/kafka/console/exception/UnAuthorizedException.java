package com.xuxd.kafka.console.exception;

/**
 * @author: xuxd
 * @date: 2023/5/17 23:08
 **/
public class UnAuthorizedException extends RuntimeException{

    public UnAuthorizedException(String message) {
        super(message);
    }
}
