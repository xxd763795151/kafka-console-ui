package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: xuxd
 * @date: 2023/1/10 20:12
 **/
@Data
public class AlterClientQuotaDTO {

    private String type;

    private List<String> types;

    private List<String> names;

    private String consumerRate;

    private String producerRate;

    private String requestPercentage;

    private List<String> deleteConfigs;
}
