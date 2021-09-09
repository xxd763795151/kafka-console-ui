package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.vo.TopicDescriptionVO;
import com.xuxd.kafka.console.service.TopicService;
import java.util.Comparator;
import java.util.List;
import kafka.console.TopicConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.TopicDescription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:02:56
 **/
@Slf4j
@Service
public class TopicServiceImpl implements TopicService {

    @Autowired
    private TopicConsole topicConsole;

    @Override public ResponseData getTopicNameList() {
        return ResponseData.create().data(topicConsole.getTopicNameList()).success();
    }

    @Override public ResponseData getTopicList() {
        List<TopicDescription> topicDescriptions = topicConsole.getTopicList(topicConsole.getTopicNameList());
        topicDescriptions.sort(Comparator.comparing(TopicDescription::name));

        return ResponseData.create().data(topicDescriptions.stream().map(d -> TopicDescriptionVO.from(d))).success();
    }
}
