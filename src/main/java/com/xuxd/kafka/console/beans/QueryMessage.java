package com.xuxd.kafka.console.beans;

import com.xuxd.kafka.console.beans.enums.FilterType;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:45:49
 **/
@Data
public class QueryMessage {

    private String topic;

    private int partition;

    private long startTime;

    private long endTime;

    private long offset;

    private String keyDeserializer;

    private String valueDeserializer;

    private FilterType filter;

    private String value;

    private String headerKey;

    private String headerValue;

    private int filterNumber;
}
