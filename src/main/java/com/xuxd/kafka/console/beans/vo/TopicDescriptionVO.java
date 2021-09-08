package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.kafka.clients.admin.TopicDescription;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 21:12:36
 **/
@Data
public class TopicDescriptionVO {

    private String name;
    private boolean internal;
    private int partitions;

    public static TopicDescriptionVO from(TopicDescription description) {
        TopicDescriptionVO vo = new TopicDescriptionVO();
        vo.setName(description.name());
        vo.setInternal(description.isInternal());
        vo.setPartitions(description.partitions().size());
        return vo;
    }
}
