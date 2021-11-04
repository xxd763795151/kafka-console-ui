package com.xuxd.kafka.console.beans.dto;

import lombok.Data;
import org.apache.kafka.clients.admin.ConfigEntry;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-04 15:39:07
 **/
@Data
public class AlterConfigDTO {

    private String entity;

    private String name;

    private String value;

    public ConfigEntry to() {
        return new ConfigEntry(name, value);
    }
}
