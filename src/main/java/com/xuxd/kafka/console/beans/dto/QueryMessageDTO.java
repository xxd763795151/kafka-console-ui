package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.QueryMessage;
import java.util.Date;
import lombok.Data;

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

        return queryMessage;
    }
}
