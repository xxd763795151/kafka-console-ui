package com.xuxd.kafka.console.config;

import kafka.console.ClusterConsole;
import kafka.console.ConfigConsole;
import kafka.console.ConsumerConsole;
import kafka.console.KafkaAclConsole;
import kafka.console.KafkaConfigConsole;
import kafka.console.MessageConsole;
import kafka.console.OperationConsole;
import kafka.console.TopicConsole;
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

    @Bean
    public TopicConsole topicConsole(KafkaConfig config) {
        return new TopicConsole(config);
    }

    @Bean
    public ConsumerConsole consumerConsole(KafkaConfig config) {
        return new ConsumerConsole(config);
    }

    @Bean
    public ClusterConsole clusterConsole(KafkaConfig config) {
        return new ClusterConsole(config);
    }

    @Bean
    public ConfigConsole configConsole(KafkaConfig config) {
        return new ConfigConsole(config);
    }

    @Bean
    public OperationConsole operationConsole(KafkaConfig config, TopicConsole topicConsole,
        ConsumerConsole consumerConsole) {
        return new OperationConsole(config, topicConsole, consumerConsole);
    }

    @Bean
    public MessageConsole messageConsole(KafkaConfig config) {
        return new MessageConsole(config);
    }
}
