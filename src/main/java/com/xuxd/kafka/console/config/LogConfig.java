package com.xuxd.kafka.console.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author: xuxd
 * @since: 2023/8/20 20:00
 **/
@Configuration
@ConfigurationProperties(prefix = "log")
public class LogConfig {

    private boolean printControllerLog = true;

    public boolean isPrintControllerLog() {
        return printControllerLog;
    }

    public void setPrintControllerLog(boolean printControllerLog) {
        this.printControllerLog = printControllerLog;
    }
}
