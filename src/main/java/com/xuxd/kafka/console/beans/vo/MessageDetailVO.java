package com.xuxd.kafka.console.beans.vo;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-12 12:45:23
 **/
@Data
public class MessageDetailVO {

    private String topic;

    private int partition;

    private long offset;

    private long timestamp;

    private String timestampType;

    private List<HeaderVO> headers = new ArrayList<>();

    private Object key;

    private Object value;

    @Data
    public static class HeaderVO {
        String key;

        String value;
    }
}
