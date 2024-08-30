package com.xuxd.kafka.console.beans.vo;

import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-22 16:24:58
 **/
@Data
public class BrokerApiVersionVO {

    private int brokerId;

    private String host;

    private int supportNums;

    private int unSupportNums;

    private List<String> versionInfo;

    private String brokerVersion;
}
