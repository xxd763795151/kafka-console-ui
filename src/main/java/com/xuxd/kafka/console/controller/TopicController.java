package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:28:35
 **/
@RestController
@RequestMapping("/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @GetMapping("/list")
    public Object getTopicList(@RequestParam(required = false) String topic, @RequestParam String type) {
        return topicService.getTopicList(topic, TopicType.valueOf(type.toUpperCase()));
    }
}
