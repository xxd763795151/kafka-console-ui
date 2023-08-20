package com.xuxd.kafka.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xuxd
 * @date: 2023/5/9 21:08
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {

    private boolean enable;

    private String secret = "kafka-console-ui-default-secret";

    private long expireHours;

    private boolean hideClusterProperty;

    private String hideClusterPropertyPerm = "op:cluster-switch:edit";
}
