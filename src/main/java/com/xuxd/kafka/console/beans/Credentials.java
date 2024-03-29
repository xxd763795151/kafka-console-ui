package com.xuxd.kafka.console.beans;

import lombok.Data;

import java.util.List;

/**
 * @author: xuxd
 * @date: 2023/5/14 19:37
 **/
@Data
public class Credentials {

    public static final Credentials INVALID = new Credentials();

    private String username;

    private long expiration;

    /**
     * 是否隐藏集群属性
     */
    private boolean hideClusterProperty;

    private List<Long> roleIdList;

    public boolean isInvalid() {
        return this == INVALID;
    }
}
