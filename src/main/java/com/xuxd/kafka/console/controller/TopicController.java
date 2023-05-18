package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.ReplicaAssignment;
import com.xuxd.kafka.console.beans.dto.AddPartitionDTO;
import com.xuxd.kafka.console.beans.dto.NewTopicDTO;
import com.xuxd.kafka.console.beans.dto.TopicThrottleDTO;
import com.xuxd.kafka.console.beans.enums.TopicType;
import com.xuxd.kafka.console.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    @GetMapping
    public Object getTopicNameList() {
        return topicService.getTopicNameList(false);
    }

    @Permission("topic:load")
    @GetMapping("/list")
    public Object getTopicList(@RequestParam(required = false) String topic, @RequestParam String type) {
        return topicService.getTopicList(topic, TopicType.valueOf(type.toUpperCase()));
    }

    @Permission({"topic:batch-del", "topic:del"})
    @DeleteMapping
    public Object deleteTopic(@RequestBody List<String> topics) {
        return topicService.deleteTopics(topics);
    }

    @Permission("topic:partition-detail")
    @GetMapping("/partition")
    public Object getTopicPartitionInfo(@RequestParam String topic) {
        return topicService.getTopicPartitionInfo(topic.trim());
    }

    @Permission("topic:add")
    @PostMapping("/new")
    public Object createNewTopic(@RequestBody NewTopicDTO topicDTO) {
        return topicService.createTopic(topicDTO.toNewTopic());
    }

    @Permission("topic:partition-add")
    @PostMapping("/partition/new")
    public Object addPartition(@RequestBody AddPartitionDTO partitionDTO) {
        String topic = partitionDTO.getTopic().trim();
        int addNum = partitionDTO.getAddNum();
        Map<Integer, List<Integer>> assignmentMap = partitionDTO.getAssignment();
        List<List<Integer>> assignment = Collections.emptyList();

        if (!assignmentMap.isEmpty()) {
            assignment = new ArrayList<>(addNum);
            for (int i = 1; i <= addNum; i++) {
                assignment.add(assignmentMap.containsKey(i) ? assignmentMap.get(i) : Collections.emptyList());
            }
        }

        return topicService.addPartitions(topic, addNum, assignment);
    }

    @GetMapping("/replica/assignment")
    public Object getCurrentReplicaAssignment(@RequestParam String topic) {
        return topicService.getCurrentReplicaAssignment(topic);
    }

    @Permission({"topic:replication-modify", "op:replication-reassign"})
    @PostMapping("/replica/assignment")
    public Object updateReplicaAssignment(@RequestBody ReplicaAssignment assignment) {
        return topicService.updateReplicaAssignment(assignment);
    }

    @Permission("topic:replication-sync-throttle")
    @PostMapping("/replica/throttle")
    public Object configThrottle(@RequestBody TopicThrottleDTO dto) {
        return topicService.configThrottle(dto.getTopic(), dto.getPartitions(), dto.getOperation());
    }

    @Permission("topic:send-count")
    @GetMapping("/send/stats")
    public Object sendStats(@RequestParam String topic) {
        return topicService.sendStats(topic);
    }
}
