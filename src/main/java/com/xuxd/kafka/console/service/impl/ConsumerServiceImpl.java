package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.CounterList;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.vo.ConsumerGroupVO;
import com.xuxd.kafka.console.service.ConsumerService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.ConsumerConsole;
import org.apache.kafka.common.ConsumerGroupState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Override public ResponseData getConsumerGroupList(List<String> groupIds, Set<ConsumerGroupState> states) {
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
        }
        List<ConsumerGroupVO> consumerGroupVOS = consumerConsole.getConsumerGroupList(groupList).stream().map(c -> ConsumerGroupVO.from(c)).collect(Collectors.toList());
        return ResponseData.create().data(new CounterList<>(consumerGroupVOS)).success();
    }
}
