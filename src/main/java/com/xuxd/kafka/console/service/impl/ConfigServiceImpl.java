package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.service.ConfigService;
import java.util.List;
import kafka.console.ConfigConsole;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-02 19:57:57
 **/
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigConsole configConsole;

    @Override public ResponseData getTopicConfig(String topic) {
        List<ConfigEntry> configEntries = configConsole.getTopicConfig(topic);
        return ResponseData.create().success();
    }

    @Override public ResponseData getBrokerConfig() {
        List<ConfigEntry> configEntries = configConsole.getBrokerConfig();
        return ResponseData.create().success();
    }
}
