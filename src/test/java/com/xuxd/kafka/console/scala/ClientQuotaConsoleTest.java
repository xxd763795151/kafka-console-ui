package com.xuxd.kafka.console.scala;

import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.config.ContextConfigHolder;
import com.xuxd.kafka.console.config.KafkaConfig;
import kafka.console.ClientQuotaConsole;
import kafka.server.ConfigType;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

public class ClientQuotaConsoleTest {

    @Test
    void testGetClientQuotasConfigs() {
        ClientQuotaConsole console = new ClientQuotaConsole(new KafkaConfig());
        ContextConfig config = new ContextConfig();
        config.setBootstrapServer("10.1.18.222:9092");
        ContextConfigHolder.CONTEXT_CONFIG.set(config);
        Map<ClientQuotaEntity, Map<String, Object>> configs = console.getClientQuotasConfigs(Arrays.asList(ConfigType.User()), Arrays.asList());
        configs.forEach((k, v) -> {
            System.out.println(k);
            System.out.println(v);
        });
    }
}
