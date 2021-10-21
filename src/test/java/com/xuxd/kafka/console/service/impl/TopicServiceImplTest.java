package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.service.TopicService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:04:28
 **/
@Slf4j
@SpringBootTest
public class TopicServiceImplTest {

    @Autowired
    private TopicService topicService;

    @Test
    public void getTopicNameList() {
        log.info(topicService.getTopicNameList(true).getData().toString());
    }

    @Test
    public void getTopicList() {
        log.info(topicService.getTopicList(null, TopicType.ALL).getData().toString());
    }
}