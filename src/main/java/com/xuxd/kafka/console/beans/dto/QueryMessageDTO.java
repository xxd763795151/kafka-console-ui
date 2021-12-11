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

    public QueryMessage toQueryMessage() {
        QueryMessage queryMessage = new QueryMessage();
        queryMessage.setTopic(topic);
        queryMessage.setPartition(partition);
        queryMessage.setStartTime(startTime.getTime());
        queryMessage.setEndTime(endTime.getTime());

        return queryMessage;
    }
}
