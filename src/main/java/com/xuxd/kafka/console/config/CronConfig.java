package com.xuxd.kafka.console.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-06 16:23:30
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "cron")
public class CronConfig {

    private String clearDirtyUser;
}
