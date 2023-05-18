package com.xuxd.kafka.console.filter;

import com.xuxd.kafka.console.beans.Credentials;

/**
 * @author: xuxd
 * @date: 2023/5/17 23:02
 **/
public class CredentialsContext {
    private static final ThreadLocal<Credentials> CREDENTIALS = new ThreadLocal<>();

    public static void set(Credentials credentials) {
        CREDENTIALS.set(credentials);
    }

    public static Credentials get() {
        return CREDENTIALS.get();
    }

    public static void remove() {
        CREDENTIALS.remove();
    }
}
