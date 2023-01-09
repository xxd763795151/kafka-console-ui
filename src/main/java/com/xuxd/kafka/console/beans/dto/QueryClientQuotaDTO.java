package com.xuxd.kafka.console.beans.dto;

import lombok.Data;

import java.util.List;

/**
 * @author: xuxd
 * @date: 2023/1/9 21:53
 **/
@Data
public class QueryClientQuotaDTO {

    private List<String> types;

    private List<String> names;
}
