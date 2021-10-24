package com.xuxd.kafka.console.beans.dto;

import java.util.Properties;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:10:47
 **/
@Data
public class SyncDataDTO {

    private String address;

    private String groupId;

    private String topic;

    private Properties properties = new Properties();
}
