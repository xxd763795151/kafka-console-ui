package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AlterConfigDTO;
import com.xuxd.kafka.console.beans.enums.AlterType;
import com.xuxd.kafka.console.config.KafkaConfig;
import com.xuxd.kafka.console.service.ConfigService;
import com.xuxd.kafka.console.utils.ConvertUtil;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 16:08:22
 **/
@RestController
@RequestMapping("/config")
public class ConfigController {

    private final KafkaConfig config;

    private final Map<String, Object> configMap;

    private final ConfigService configService;

    public ConfigController(KafkaConfig config, ConfigService configService) {
        this.config = config;
        this.configMap = ConvertUtil.toMap(config);
        this.configService = configService;
    }

    @GetMapping
    public Object getConfig() {
        return ResponseData.create().data(configMap).success();
    }

    @GetMapping("/topic")
    public Object getTopicConfig(String topic) {
        return configService.getTopicConfig(topic);
    }

    @GetMapping("/broker")
    public Object getBrokerConfig(String brokerId) {
        return configService.getBrokerConfig(brokerId);
    }

    @PostMapping("/broker")
    public Object setBrokerConfig(@RequestBody AlterConfigDTO dto) {
        return configService.alterBrokerConfig(dto.getEntity(), dto.to(), AlterType.SET);
    }

    @DeleteMapping("/broker")
    public Object deleteBrokerConfig(@RequestBody AlterConfigDTO dto) {
        return configService.alterBrokerConfig(dto.getEntity(), dto.to(), AlterType.DELETE);
    }
}
