package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import java.util.List;
import java.util.Set;
import org.apache.kafka.common.ConsumerGroupState;

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
}
