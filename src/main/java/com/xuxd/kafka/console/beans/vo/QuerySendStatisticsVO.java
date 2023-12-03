package com.xuxd.kafka.console.beans.vo;

import lombok.Data;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author: xuxd
 * @since: 2023/12/1 17:49
 **/
@Data
public class QuerySendStatisticsVO {

    private static final String FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat(FORMAT);

    private String topic;

    private Long total;

    private Map<Integer, Long> detail;

    private String startTime;

    private String endTime;

    private String searchTime = format(new Date());

    public static String format(Date date) {
        return DATE_FORMAT.format(date);
    }
}
