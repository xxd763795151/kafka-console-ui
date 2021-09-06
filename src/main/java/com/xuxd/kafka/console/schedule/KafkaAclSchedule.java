package com.xuxd.kafka.console.schedule;

import com.xuxd.kafka.console.dao.KafkaUserMapper;
import java.util.Set;
import kafka.console.KafkaConfigConsole;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-06 16:22:33
 **/
@Slf4j
@Component
public class KafkaAclSchedule {

    private final KafkaUserMapper userMapper;

    private final KafkaConfigConsole configConsole;

    public KafkaAclSchedule(KafkaUserMapper userMapper, KafkaConfigConsole configConsole) {
        this.userMapper = userMapper;
        this.configConsole = configConsole;
    }

    @Scheduled(cron = "${cron.clear-dirty-user}")
    public void clearDirtyKafkaUser() {
        log.info("Start clear dirty data for kafka user from database.");
        Set<String> userSet = configConsole.getUserList(null);
        userMapper.selectList(null).forEach(u -> {
            if (!userSet.contains(u.getUsername())) {
                log.info("clear user: {} from database.", u.getUsername());
                try {
                    userMapper.deleteById(u.getId());
                } catch (Exception e) {
                    log.error("userMapper.deleteById error, user: " + u, e);
                }
            }
        });
        log.info("Clear end.");
    }
}
