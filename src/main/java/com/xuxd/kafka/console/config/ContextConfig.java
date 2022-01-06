package com.xuxd.kafka.console.config;

import java.util.Properties;
import org.apache.kafka.clients.CommonClientConfigs;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-30 15:46:55
 **/
public class ContextConfig {

    public static final int DEFAULT_REQUEST_TIMEOUT_MS = 5000;

    private Long clusterInfoId;

    private String clusterName;

    private String bootstrapServer;

    private int requestTimeoutMs = DEFAULT_REQUEST_TIMEOUT_MS;

    private Properties properties = new Properties();

    public String getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public int getRequestTimeoutMs() {
        return properties.containsKey(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG) ?
            Integer.parseInt(properties.getProperty(CommonClientConfigs.REQUEST_TIMEOUT_MS_CONFIG)) : requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public Properties getProperties() {
        return properties;
    }

    public Long getClusterInfoId() {
        return clusterInfoId;
    }

    public void setClusterInfoId(Long clusterInfoId) {
        this.clusterInfoId = clusterInfoId;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override public String toString() {
        return "KafkaContextConfig{" +
            "bootstrapServer='" + bootstrapServer + '\'' +
            ", requestTimeoutMs=" + requestTimeoutMs +
            ", properties=" + properties +
            '}';
    }
}
