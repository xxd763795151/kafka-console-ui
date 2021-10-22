package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-22 16:21:28
 **/
@Data
public class ResetOffsetDTO {

    // 重置粒度：1-> topic，2->partition
    private int level;

    // 重置类型：1-> earliest, 2-> latest， 3-> timestamp
    private int type;

    private String groupId;

    private String topic;

    private int partition;

    public interface Level {
        int TOPIC = 1;
        int PARTITION = 2;
    }

    public interface Type {
        int EARLIEST = 1;
        int LATEST = 2;
        int TIMESTAMP = 3;
    }
}
