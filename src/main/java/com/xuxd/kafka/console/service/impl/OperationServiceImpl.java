package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.service.OperationService;
import java.util.Properties;
import kafka.console.OperationConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:12:54
 **/
@Service
public class OperationServiceImpl implements OperationService {

    @Autowired
    private OperationConsole operationConsole;

    @Override public ResponseData syncConsumerOffset(String groupId, String topic, Properties thatProps) {

        Tuple2<Object, String> tuple2 = operationConsole.syncConsumerOffset(groupId, topic, thatProps);

        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }
}
