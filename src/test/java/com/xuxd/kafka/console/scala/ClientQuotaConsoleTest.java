package com.xuxd.kafka.console.scala;

import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.config.ContextConfigHolder;
import com.xuxd.kafka.console.config.KafkaConfig;
import kafka.console.ClientQuotaConsole;
import org.apache.kafka.common.config.internals.QuotaConfigs;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ClientQuotaConsoleTest {

    String bootstrapServer = "127.0.0.1:9092";

    @Test
    void testGetClientQuotasConfigs() {
        ClientQuotaConsole console = new ClientQuotaConsole(new KafkaConfig());
        ContextConfig config = new ContextConfig();
        config.setBootstrapServer(bootstrapServer);
        ContextConfigHolder.CONTEXT_CONFIG.set(config);
        Map<ClientQuotaEntity, Map<String, Object>> configs = console.getClientQuotasConfigs(Arrays.asList(ClientQuotaEntity.USER), Arrays.asList());
        configs.forEach((k, v) -> {
            System.out.println(k);
            System.out.println(v);
        });
    }

    @Test
    void testAlterClientQuotasConfigs() {
        ClientQuotaConsole console = new ClientQuotaConsole(new KafkaConfig());
        ContextConfig config = new ContextConfig();
        config.setBootstrapServer(bootstrapServer);
        ContextConfigHolder.CONTEXT_CONFIG.set(config);
        Map<String, String> configsToBeAddedMap = new HashMap<>();
        configsToBeAddedMap.put(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, "102400");

        console.addQuotaConfigs(Arrays.asList(ClientQuotaEntity.USER), Arrays.asList("user-test"), configsToBeAddedMap);
    }
}
