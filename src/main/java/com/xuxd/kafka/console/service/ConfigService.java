package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-02 19:57:43
 **/
public interface ConfigService {

    ResponseData getTopicConfig(String topic);

    ResponseData getBrokerConfig();
}
