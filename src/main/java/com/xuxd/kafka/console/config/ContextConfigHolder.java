package com.xuxd.kafka.console.config;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-30 18:55:28
 **/
public class ContextConfigHolder {

    public static final ThreadLocal<ContextConfig> CONTEXT_CONFIG = new ThreadLocal<>();
}
