package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.TopicType;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 20:01:49
 **/
public interface TopicService {

    ResponseData getTopicNameList(boolean internal);

    ResponseData getTopicList(String topic, TopicType type);

    ResponseData deleteTopic(String topic);

    ResponseData getTopicPartitionInfo(String topic);

    ResponseData createTopic(NewTopic topic);

    ResponseData addPartitions(String topic, int addNum, List<List<Integer>> newAssignmentst);
}
