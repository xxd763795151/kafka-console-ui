package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AddSubscriptionDTO;
import com.xuxd.kafka.console.beans.dto.QueryConsumerGroupDTO;
import com.xuxd.kafka.console.beans.dto.ResetOffsetDTO;
import com.xuxd.kafka.console.service.ConsumerService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.ConsumerGroupState;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-11 11:16:09
 **/
@RestController
@RequestMapping("/consumer")
public class ConsumerController {

    @Autowired
    private ConsumerService consumerService;

    @PostMapping("/group/list")
    public Object getGroupList(@RequestBody(required = false) QueryConsumerGroupDTO dto) {
        if (Objects.isNull(dto)) {
            return consumerService.getConsumerGroupList(null, null);
        }
        List<String> groupIdList = StringUtils.isNotBlank(dto.getGroupId()) ? Collections.singletonList(dto.getGroupId()) : Collections.emptyList();

        Set<ConsumerGroupState> stateSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dto.getStates())) {
            dto.getStates().stream().forEach(s -> stateSet.add(ConsumerGroupState.valueOf(s.toUpperCase())));
        }
        return consumerService.getConsumerGroupList(groupIdList, stateSet);
    }

    @ControllerLog("删除消费组")
    @Permission("group:del")
    @DeleteMapping("/group")
    public Object deleteConsumerGroup(@RequestParam("groupId") String groupId) {
        return consumerService.deleteConsumerGroup(groupId);
    }

    @Permission("group:client")
    @GetMapping("/member")
    public Object getConsumerMembers(@RequestParam("groupId") String groupId) {
        return consumerService.getConsumerMembers(groupId);
    }

    @Permission("group:consumer-detail")
    @GetMapping("/detail")
    public Object getConsumerDetail(@RequestParam("groupId") String groupId) {
        return consumerService.getConsumerDetail(groupId);
    }

    @ControllerLog("新增消费组")
    @Permission("group:add")
    @PostMapping("/subscription")
    public Object addSubscription(@RequestBody AddSubscriptionDTO subscriptionDTO) {
        return consumerService.addSubscription(subscriptionDTO.getGroupId(), subscriptionDTO.getTopic());
    }

    @ControllerLog("重置消费位点")
    @Permission({"group:consumer-detail:min",
            "group:consumer-detail:last",
            "group:consumer-detail:timestamp",
            "group:consumer-detail:any"})
    @PostMapping("/reset/offset")
    public Object restOffset(@RequestBody ResetOffsetDTO offsetDTO) {
        ResponseData res = ResponseData.create().failed("unknown");
        switch (offsetDTO.getLevel()) {
            case ResetOffsetDTO.Level.TOPIC:
                switch (offsetDTO.getType()) {
                    case ResetOffsetDTO.Type
                            .EARLIEST:
                        res = consumerService.resetOffsetToEndpoint(offsetDTO.getGroupId(), offsetDTO.getTopic(), OffsetResetStrategy.EARLIEST);
                        break;
                    case ResetOffsetDTO.Type.LATEST:
                        res = consumerService.resetOffsetToEndpoint(offsetDTO.getGroupId(), offsetDTO.getTopic(), OffsetResetStrategy.LATEST);
                        break;
                    case ResetOffsetDTO.Type.TIMESTAMP:
                        res = consumerService.resetOffsetByDate(offsetDTO.getGroupId(), offsetDTO.getTopic(), offsetDTO.getDateStr());
                        break;
                    default:
                        return ResponseData.create().failed("unknown type");
                }
                break;
            case ResetOffsetDTO.Level.PARTITION:
                switch (offsetDTO.getType()) {
                    case ResetOffsetDTO.Type
                            .SPECIAL:
                        res = consumerService.resetPartitionToTargetOffset(offsetDTO.getGroupId(), new TopicPartition(offsetDTO.getTopic(), offsetDTO.getPartition()), offsetDTO.getOffset());
                        break;
                    default:
                        return ResponseData.create().failed("unknown type");
                }
                break;
            default:
                return ResponseData.create().failed("unknown level");
        }

        return res;
    }

    @GetMapping("/group/id/list")
    public Object getGroupIdList() {
        return consumerService.getGroupIdList();
    }

    @GetMapping("/topic/list")
    public Object getSubscribeTopicList(@RequestParam("groupId") String groupId) {
        return consumerService.getSubscribeTopicList(groupId);
    }

    @Permission({"topic:consumer-detail"})
    @GetMapping("/topic/subscribed")
    public Object getTopicSubscribedByGroups(@RequestParam("topic") String topic) {
        return consumerService.getTopicSubscribedByGroups(topic);
    }

    @Permission("group:offset-partition")
    @GetMapping("/offset/partition")
    public Object getOffsetPartition(@RequestParam("groupId") String groupId) {
        return consumerService.getOffsetPartition(groupId);
    }
}
