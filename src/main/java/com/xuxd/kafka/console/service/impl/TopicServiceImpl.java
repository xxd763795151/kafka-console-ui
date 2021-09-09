package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.beans.vo.TopicDescriptionVO;
import com.xuxd.kafka.console.service.TopicService;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import kafka.console.TopicConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        return ResponseData.create().data(topicConsole.getTopicNameList(true)).success();
    }

    @Override public ResponseData getTopicList(String topic, TopicType type) {
        Set<String> topicSet = new HashSet<>();
        switch (type) {
            case SYSTEM:
                Set<String> internalTopicSet = topicConsole.getInternalTopicNameList();
                if (StringUtils.isEmpty(topic)) {
                    topicSet.addAll(internalTopicSet);
                } else {
                    if (internalTopicSet.contains(topic)) {
                        topicSet.add(topic);
                    } else {
                        return ResponseData.create().data(Collections.emptyList()).success();
                    }
                }
                break;
            case NORMAL:
                Set<String> internalTopicS = topicConsole.getInternalTopicNameList();
                if (internalTopicS.contains(topic)) {
                    return ResponseData.create().data(Collections.emptyList()).success();
                }
                topicSet.addAll(StringUtils.isEmpty(topic) ? topicConsole.getTopicNameList(false) : Collections.singleton(topic));
                break;
            default:
                topicSet.addAll(StringUtils.isEmpty(topic) ? topicConsole.getTopicNameList(true) : Collections.singleton(topic));
                break;
        }
        List<TopicDescription> topicDescriptions = topicConsole.getTopicList(topicSet);
        topicDescriptions.sort(Comparator.comparing(TopicDescription::name));

        return ResponseData.create().data(topicDescriptions.stream().map(d -> TopicDescriptionVO.from(d))).success();
    }
}
