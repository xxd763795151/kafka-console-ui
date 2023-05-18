package com.xuxd.kafka.console.beans;

import lombok.Data;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:37
 **/
@Data
public class Credentials {

    public static final Credentials INVALID = new Credentials();

    private String username;

    private long expiration;

    public boolean isInvalid() {
        return this == INVALID;
    }
}
