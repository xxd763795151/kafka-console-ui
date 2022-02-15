package com.xuxd.kafka.console.beans.dto;

import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-02-15 19:08:13
 **/
@Data
public class ProposedAssignmentDTO {

    private String topic;

    private List<Integer> brokers;
}
