package com.xuxd.kafka.console.beans.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-18 19:55:40
 **/
@Data
public class AddPartitionDTO {

    private String topic;

    private int addNum;

    private List<List<Integer>> assignment = new ArrayList<>();
}
