package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:01:49
 **/
public interface TopicService {

    ResponseData getTopicNameList();

    ResponseData getTopicList();

}
