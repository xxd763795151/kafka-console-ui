package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.service.ConsumerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-10 20:11:22
 **/
@SpringBootTest
public class ConsumerServiceImplTest {

    @Autowired
    private ConsumerService consumerService;

    @Test
    public void getConsumerGroupList() {
        Object data = consumerService.getConsumerGroupList(null, null).getData();
        System.out.println(data);
    }
}