package com.xuxd.kafka.console.config;

import java.util.Properties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:21:12
 **/
@Configuration
@ConfigurationProperties(prefix = "kafka.config")
public class KafkaConfig {

    private String bootstrapServer;

    private String zookeeperAddr;

    private Properties properties;

    private boolean cacheAdminConnection;

    private boolean cacheProducerConnection;

    private boolean cacheConsumerConnection;

    public String getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public String getZookeeperAddr() {
        return zookeeperAddr;
    }

    public void setZookeeperAddr(String zookeeperAddr) {
        this.zookeeperAddr = zookeeperAddr;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public boolean isCacheAdminConnection() {
        return cacheAdminConnection;
    }

    public void setCacheAdminConnection(boolean cacheAdminConnection) {
        this.cacheAdminConnection = cacheAdminConnection;
    }

    public boolean isCacheProducerConnection() {
        return cacheProducerConnection;
    }

    public void setCacheProducerConnection(boolean cacheProducerConnection) {
        this.cacheProducerConnection = cacheProducerConnection;
    }

    public boolean isCacheConsumerConnection() {
        return cacheConsumerConnection;
    }

    public void setCacheConsumerConnection(boolean cacheConsumerConnection) {
        this.cacheConsumerConnection = cacheConsumerConnection;
    }
}
