package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.enums.ThrottleUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-24 19:37:10
 **/
@Data
public class BrokerThrottleDTO {

    private List<Integer> brokerList = new ArrayList<>();

    private long throttle;

    private ThrottleUnit unit;
}
