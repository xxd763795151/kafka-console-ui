package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.enums.FilterType;
import java.util.Date;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:17:59
 **/
@Data
public class QueryMessageDTO {

    private String topic;

    private int partition;

    private Date startTime;

    private Date endTime;

    private Long offset;

    private String keyDeserializer;

    private String valueDeserializer;

    private String filter;

    private String value;

    private String headerKey;

    private String headerValue;

    public QueryMessage toQueryMessage() {
        QueryMessage queryMessage = new QueryMessage();
        queryMessage.setTopic(topic);
        queryMessage.setPartition(partition);
        if (startTime != null) {
            queryMessage.setStartTime(startTime.getTime());
        }
        if (endTime != null) {
            queryMessage.setEndTime(endTime.getTime());
        }

        if (offset != null) {
            queryMessage.setOffset(offset);
        }

        queryMessage.setKeyDeserializer(keyDeserializer);
        queryMessage.setValueDeserializer(valueDeserializer);

        if (StringUtils.isNotBlank(filter)) {
            queryMessage.setFilter(FilterType.valueOf(filter.toUpperCase()));
        } else {
            queryMessage.setFilter(FilterType.NONE);
        }
        if (StringUtils.isNotBlank(value)) {
            queryMessage.setValue(value.trim());
        }
        if (StringUtils.isNotBlank(headerKey)) {
            queryMessage.setHeaderKey(headerKey.trim());
        }
        if (StringUtils.isNotBlank(headerValue)) {
            queryMessage.setHeaderValue(headerValue.trim());
        }

        return queryMessage;
    }
}
