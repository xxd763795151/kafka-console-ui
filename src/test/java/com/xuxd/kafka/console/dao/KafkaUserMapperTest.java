package com.xuxd.kafka.console.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-06 15:14:09
 **/
@SpringBootTest
public class KafkaUserMapperTest {

    @Autowired
    private KafkaUserMapper userMapper;

    @Test
    public void testSelect() {
        userMapper.selectList(null).forEach(System.out::println);
    }
}