package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.AlterType;
import org.apache.kafka.clients.admin.ConfigEntry;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-02 19:57:43
 **/
public interface ConfigService {

    ResponseData getTopicConfig(String topic);

    ResponseData getBrokerConfig(String brokerId);

    ResponseData alterBrokerConfig(String brokerId, ConfigEntry entry, AlterType type);
}
