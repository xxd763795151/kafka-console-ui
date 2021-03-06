package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.enums.AlterType;
import com.xuxd.kafka.console.beans.vo.ConfigEntryVO;
import com.xuxd.kafka.console.service.ConfigService;
import java.util.List;
import java.util.stream.Collectors;
import kafka.console.ConfigConsole;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-11-02 19:57:57
 **/
@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigConsole configConsole;

    @Override public ResponseData getTopicConfig(String topic) {
        List<ConfigEntry> configEntries = configConsole.getTopicConfig(topic);
        List<ConfigEntryVO> vos = configEntries.stream().map(ConfigEntryVO::from).sorted().collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override public ResponseData getBrokerConfig(String brokerId) {
        List<ConfigEntry> configEntries = configConsole.getBrokerConfig(brokerId);
        List<ConfigEntryVO> vos = configEntries.stream().map(ConfigEntryVO::from).sorted().collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override public ResponseData getBrokerLoggerConfig(String brokerId) {
        List<ConfigEntry> configEntries = configConsole.getBrokerLoggerConfig(brokerId);
        List<ConfigEntryVO> vos = configEntries.stream().map(ConfigEntryVO::from).sorted().collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override public ResponseData alterBrokerConfig(String brokerId, ConfigEntry entry, AlterType type) {
        Tuple2<Object, String> tuple2 = null;
        switch (type) {
            case SET:
                tuple2 = configConsole.setBrokerConfig(brokerId, entry);
                break;
            case DELETE:
                tuple2 = configConsole.deleteBrokerConfig(brokerId, entry);
                break;
        }
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData alterBrokerLoggerConfig(String brokerId, ConfigEntry entry, AlterType type) {
        Tuple2<Object, String> tuple2 = null;
        switch (type) {
            case SET:
                tuple2 = configConsole.setBrokerLoggerConfig(brokerId, entry);
                break;
            case DELETE:
                tuple2 = configConsole.deleteBrokerLoggerConfig(brokerId, entry);
                break;
        }
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override
    public ResponseData alterTopicConfig(String topic, ConfigEntry entry, AlterType type) {
        Tuple2<Object, String> tuple2 = null;
        switch (type) {
            case SET:
                tuple2 = configConsole.setTopicConfig(topic, entry);
                break;
            case DELETE:
                tuple2 = configConsole.deleteTopicConfig(topic, entry);
                break;
        }
        return (boolean) tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

}
