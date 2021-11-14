package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.CounterList;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.vo.ConsumerDetailVO;
import com.xuxd.kafka.console.beans.vo.ConsumerGroupVO;
import com.xuxd.kafka.console.beans.vo.ConsumerMemberVO;
import com.xuxd.kafka.console.service.ConsumerService;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.ConsumerConsole;
import kafka.console.TopicConsole;
import org.apache.commons.collections.CollectionUtils;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-10 19:40:10
 **/
@Service
public class ConsumerServiceImpl implements ConsumerService {

    @Autowired
    private ConsumerConsole consumerConsole;

    private TopicSubscribedInfo topicSubscribedInfo = new TopicSubscribedInfo();

    @Autowired
    private TopicConsole topicConsole;

    @Override public ResponseData getConsumerGroupList(List<String> groupIds, Set<ConsumerGroupState> states) {
        String simulateGroup = "inner_xxx_not_exit_group_###" + System.currentTimeMillis();
        Set<String> groupList = new HashSet<>();
        if (groupIds != null && !groupIds.isEmpty()) {
            if (states != null && !states.isEmpty()) {
                Set<String> stateGroup = consumerConsole.getConsumerGroupIdList(states);
                Set<String> filterGroupList = groupIds.stream().filter(x -> stateGroup.contains(x)).collect(Collectors.toSet());
                if (filterGroupList.isEmpty()) {
                    return ResponseData.create().data(Collections.emptyList()).success();
                } else {
                    groupList.addAll(filterGroupList);
                }
            } else {
                groupList.addAll(groupIds);
            }
        } else {
            groupList.addAll(consumerConsole.getConsumerGroupIdList(states));
            if (groupList.isEmpty()) {
                // The consumer groupId that match the specified states could not find, so simulate an impossible groupId.
                groupList.add(simulateGroup);
            }
        }
        List<ConsumerGroupVO> consumerGroupVOS = consumerConsole.getConsumerGroupList(groupList).stream().map(c -> ConsumerGroupVO.from(c)).collect(Collectors.toList());
        if (consumerGroupVOS.size() == 1 && consumerGroupVOS.get(0).getGroupId().equals(simulateGroup)) {
            consumerGroupVOS.clear();
        }
        consumerGroupVOS.sort(Comparator.comparing(ConsumerGroupVO::getGroupId));
        return ResponseData.create().data(new CounterList<>(consumerGroupVOS)).success();
    }

    @Override public ResponseData deleteConsumerGroup(String groupId) {
        Tuple2<Object, String> tuple2 = consumerConsole.deleteConsumerGroups(Collections.singletonList(groupId));
        return (Boolean) tuple2._1 ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2);
    }

    @Override public ResponseData getConsumerMembers(String groupId) {
        Set<ConsumerGroupDescription> groupList = consumerConsole.getConsumerGroupList(Collections.singleton(groupId));
        if (CollectionUtils.isEmpty(groupList)) {
            return ResponseData.create().data(Collections.emptyList()).success();
        }
        Collection<MemberDescription> members = groupList.stream().findFirst().get().members();
        List<ConsumerMemberVO> vos = members.stream().map(ConsumerMemberVO::from).collect(Collectors.toList());
        vos.sort(Comparator.comparing(ConsumerMemberVO::getClientId));
        return ResponseData.create().data(vos).success();
    }

    @Override public ResponseData getConsumerDetail(String groupId) {
        Collection<ConsumerConsole.TopicPartitionConsumeInfo> consumerDetail = consumerConsole.getConsumerDetail(Collections.singleton(groupId));

        List<ConsumerDetailVO> collect = consumerDetail.stream().map(ConsumerDetailVO::from).collect(Collectors.toList());
        Map<String, List<ConsumerDetailVO>> map = collect.stream().collect(Collectors.groupingBy(ConsumerDetailVO::getTopic));

        Map<String, Object> res = new HashMap<>();
        map.forEach((topic, list) -> {
            Map<String, Object> sorting = new HashMap<>();
            Collections.sort(list);
            sorting.put("data", list);
            sorting.put("lag", list.stream().map(ConsumerDetailVO::getLag).reduce(Long::sum));
            res.put(topic, sorting);
        });
        return ResponseData.create().data(res).success();
    }

    @Override public ResponseData addSubscription(String groupId, String topic) {
        // check whether exist subscription relationship.
        Collection<ConsumerConsole.TopicPartitionConsumeInfo> consumerDetail = consumerConsole.getConsumerDetail(Collections.singleton(groupId));
        if (CollectionUtils.isNotEmpty(consumerDetail)) {
            List<ConsumerConsole.TopicPartitionConsumeInfo> collect = consumerDetail.stream()
                .filter(t -> t.getTopicPartition().topic().equals(topic)).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(collect)) {
                return ResponseData.create().failed("The subscription exist.");
            }
        }

        // consumer message and commit offset.
        Tuple2<Object, String> tuple21 = consumerConsole.consumeMessageDoNothing(groupId, topic);
        if (!(boolean) tuple21._1()) {
            return ResponseData.create().failed(tuple21._2());
        }

        // reset consume offset to earliest.
        Tuple2<Object, String> tuple2 = consumerConsole.resetOffsetToEarliest(groupId, topic);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData resetOffsetToEndpoint(String groupId, String topic, OffsetResetStrategy strategy) {
        Tuple2<Object, String> tuple2 = consumerConsole.resetOffsetToEndpoint(groupId, topic, strategy);
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData resetOffsetByDate(String groupId, String topic, String dateStr) {
        long timestamp = -1L;
        try {
            StringBuilder sb = new StringBuilder(dateStr.replace(" ", "T")).append(".000");
            timestamp = Utils.getDateTime(sb.toString());
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
        List<TopicPartition> partitions = consumerConsole.listSubscribeTopics(groupId).get(topic);
        Tuple2<Object, String> tuple2 = consumerConsole.resetOffsetByTimestamp(groupId, partitions, timestamp);
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData resetPartitionToTargetOffset(String groupId, TopicPartition partition, long offset) {
        Tuple2<Object, String> tuple2 = consumerConsole.resetPartitionToTargetOffset(groupId, partition, offset);
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData getGroupIdList() {
        Set<String> stateGroup = consumerConsole.getConsumerGroupIdList(null);
        return ResponseData.create().data(stateGroup).success();
    }

    @Override public ResponseData getSubscribeTopicList(String groupId) {
        return ResponseData.create().data(consumerConsole.listSubscribeTopics(groupId).keySet()).success();
    }

    @Override public ResponseData getTopicSubscribedByGroups(String topic) {
        if (topicSubscribedInfo.isNeedRefresh(topic)) {
            Set<String> groupIdList = consumerConsole.getConsumerGroupIdList(Collections.emptySet());
            Map<String, Set<String>> cache = new HashMap<>();
            Map<String, List<TopicPartition>> subscribeTopics = consumerConsole.listSubscribeTopics(groupIdList);

            subscribeTopics.forEach((groupId, tl) -> {
                tl.forEach(topicPartition -> {
                    String t = topicPartition.topic();
                    if (!cache.containsKey(t)) {
                        cache.put(t, new HashSet<>());
                    }
                    cache.get(t).add(groupId);
                });
            });

            topicSubscribedInfo.refresh(cache);
        }

        Set<String> groups = topicSubscribedInfo.getSubscribedGroups(topic);

        Map<String, Object> res = new HashMap<>();
        Collection<ConsumerConsole.TopicPartitionConsumeInfo> consumerDetail = consumerConsole.getConsumerDetail(groups);
        List<ConsumerDetailVO> collect = consumerDetail.stream().filter(c -> topic.equals(c.topicPartition().topic())).map(ConsumerDetailVO::from).collect(Collectors.toList());
        Map<String, List<ConsumerDetailVO>> map = collect.stream().collect(Collectors.groupingBy(ConsumerDetailVO::getGroupId));

        map.forEach((groupId, list) -> {
            Map<String, Object> sorting = new HashMap<>();
            Collections.sort(list);
            sorting.put("data", list);
            sorting.put("lag", list.stream().map(ConsumerDetailVO::getLag).reduce(Long::sum));
            res.put(groupId, sorting);
        });

        return ResponseData.create().data(res).success();
    }

    @Override public ResponseData getOffsetPartition(String groupId) {
        List<TopicDescription> topicList = topicConsole.getTopicList(Collections.singleton(Topic.GROUP_METADATA_TOPIC_NAME));
        if (topicList.isEmpty()) {
            return ResponseData.create().failed(Topic.GROUP_METADATA_TOPIC_NAME + " is null.");
        }
        int size = topicList.get(0).partitions().size();
        return ResponseData.create().data(Utils.abs(groupId.hashCode()) % size);
    }

    class TopicSubscribedInfo {
        long lastTime = System.currentTimeMillis();

        long refreshThreshold = 120 * 1000;

        Map<String, Set<String>> cache = new HashMap<>();

        public void refresh(Map<String, Set<String>> newCache) {
            cache = newCache;
            lastTime = System.currentTimeMillis();
        }

        public Set<String> getSubscribedGroups(String topic) {
            return cache.getOrDefault(topic, Collections.emptySet());
        }

        public boolean isNeedRefresh(String topic) {
            return System.currentTimeMillis() - lastTime > refreshThreshold || !cache.containsKey(topic);
        }

    }
}
