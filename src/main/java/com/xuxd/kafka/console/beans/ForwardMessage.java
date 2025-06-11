package com.xuxd.kafka.console.beans;

import lombok.Data;

/**
 * 消息转发请求参数.
 *
 * @author: xuxd
 * @since: 2025/6/5 16:52
 **/
@Data
public class ForwardMessage {

    private SendMessage message;

    /**
     * 是否还发到同一个分区.
     */
    private boolean samePartition;

    /**
     * 目标集群id.
     */
    private long targetClusterId;
}
