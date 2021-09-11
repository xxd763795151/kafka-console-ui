package com.xuxd.kafka.console.beans.dto;

import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-11 11:26:47
 **/
@Data
public class QueryConsumerGroupDTO {

    private String groupId;

    private List<String> states;
}
