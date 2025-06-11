package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ForwardMessage;
import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.dto.QuerySendStatisticsDTO;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.SendMessage;

import java.util.List;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:43:26
 **/
public interface MessageService {

    ResponseData searchByTime(QueryMessage queryMessage);

    ResponseData searchByOffset(QueryMessage queryMessage);

    ResponseData searchDetail(QueryMessage queryMessage);

    ResponseData deserializerList();

    ResponseData send(SendMessage message);

    ResponseData sendWithHeader(SendMessage message);

    ResponseData resend(SendMessage message);

    ResponseData delete(List<QueryMessage> messages);

    ResponseData sendStatisticsByTime(QuerySendStatisticsDTO request);

    ResponseData forward(ForwardMessage message);
}
