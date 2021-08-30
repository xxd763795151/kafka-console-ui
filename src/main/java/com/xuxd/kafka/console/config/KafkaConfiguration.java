package com.xuxd.kafka.console.config;

import kafka.console.KafkaAclConsole;
import kafka.console.KafkaConfigConsole;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:45:26
 **/
@Configuration
public class KafkaConfiguration {

    @Bean
    public KafkaConfigConsole kafkaConfigConsole(KafkaConfig config) {
        return new KafkaConfigConsole(config);
    }

    @Bean
    public KafkaAclConsole kafkaAclConsole(KafkaConfig config) {
        return new KafkaAclConsole(config);
    }
}
