package com.xuxd.kafka.console.config;

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

    private int requestTimeoutMs;

    private String securityProtocol;

    private String saslMechanism;

    private String saslJaasConfig;

    public String getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(String bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

    public int getRequestTimeoutMs() {
        return requestTimeoutMs;
    }

    public void setRequestTimeoutMs(int requestTimeoutMs) {
        this.requestTimeoutMs = requestTimeoutMs;
    }

    public String getSecurityProtocol() {
        return securityProtocol;
    }

    public void setSecurityProtocol(String securityProtocol) {
        this.securityProtocol = securityProtocol;
    }

    public String getSaslMechanism() {
        return saslMechanism;
    }

    public void setSaslMechanism(String saslMechanism) {
        this.saslMechanism = saslMechanism;
    }

    public String getSaslJaasConfig() {
        return saslJaasConfig;
    }

    public void setSaslJaasConfig(String saslJaasConfig) {
        this.saslJaasConfig = saslJaasConfig;
    }
}
