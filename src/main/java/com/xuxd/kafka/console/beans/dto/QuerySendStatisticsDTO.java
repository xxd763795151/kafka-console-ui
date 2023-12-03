package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

import java.util.Date;
import java.util.Set;

/**
 * 发送统计查询请求，指定时间段内，发送了多少消息.
 * @author: xuxd
 * @since: 2023/12/1 22:01
 **/
@Data
public class QuerySendStatisticsDTO {

    private String topic;

    private Set<Integer> partition;

    private Date startTime;

    private Date endTime;
}
