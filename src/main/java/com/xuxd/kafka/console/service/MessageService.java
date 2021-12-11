package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.ResponseData;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:43:26
 **/
public interface MessageService {

    ResponseData searchByTime(QueryMessage queryMessage);

    ResponseData searchByOffset(QueryMessage queryMessage);
}
