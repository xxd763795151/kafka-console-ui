package com.xuxd.kafka.console.beans.dto;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.apache.commons.collections.MapUtils;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-13 14:58:11
 **/
@Data
public class NewTopicDTO {

    private String name;
    private Integer numPartitions;
    private Short replicationFactor;
    private Map<String, String> configs = new HashMap<>();

    public NewTopic toNewTopic() {
        NewTopic topic = new NewTopic(name, numPartitions, replicationFactor);
        if (MapUtils.isNotEmpty(configs)) {
            topic.configs(configs);
        }
        return topic;
    }
}
