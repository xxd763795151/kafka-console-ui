package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import java.util.List;
import java.util.Set;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-10 19:39:26
 **/
public interface ConsumerService {

    ResponseData getConsumerGroupList(List<String> groupIds, Set<ConsumerGroupState> states);

    ResponseData deleteConsumerGroup(String groupId);

    ResponseData getConsumerMembers(String groupId);

    ResponseData getConsumerDetail(String groupId);

    ResponseData addSubscription(String groupId, String topic);

    ResponseData resetOffsetToEndpoint(String groupId, String topic, OffsetResetStrategy strategy);

    ResponseData resetOffsetByDate(String groupId, String topic, String dateStr);

    ResponseData resetPartitionToTargetOffset(String groupId, TopicPartition partition, long offset);

    ResponseData getGroupIdList();

    ResponseData getSubscribeTopicList(String groupId);

    ResponseData getTopicSubscribedByGroups(String topic);
}
