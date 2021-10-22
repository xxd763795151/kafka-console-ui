package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AddSubscriptionDTO;
import com.xuxd.kafka.console.beans.dto.QueryConsumerGroupDTO;
import com.xuxd.kafka.console.beans.dto.ResetOffsetDTO;
import com.xuxd.kafka.console.service.ConsumerService;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.ConsumerGroupState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @DeleteMapping("/group")
    public Object deleteConsumerGroup(@RequestParam String groupId) {
        return consumerService.deleteConsumerGroup(groupId);
    }

    @GetMapping("/member")
    public Object getConsumerMembers(@RequestParam String groupId) {
        return consumerService.getConsumerMembers(groupId);
    }

    @GetMapping("/detail")
    public Object getConsumerDetail(@RequestParam String groupId) {
        return consumerService.getConsumerDetail(groupId);
    }

    @PostMapping("/subscription")
    public Object addSubscription(@RequestBody AddSubscriptionDTO subscriptionDTO) {
        return consumerService.addSubscription(subscriptionDTO.getGroupId(), subscriptionDTO.getTopic());
    }

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
                        break;
                    default:
                        return ResponseData.create().failed("unknown type");
                }
                break;
            case ResetOffsetDTO.Level.PARTITION:
                break;
            default:
                return ResponseData.create().failed("unknown level");
        }

        return res;
    }
}
