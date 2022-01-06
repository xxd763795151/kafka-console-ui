package com.xuxd.kafka.console.schedule;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.dos.KafkaUserDO;
import com.xuxd.kafka.console.config.ContextConfig;
import com.xuxd.kafka.console.config.ContextConfigHolder;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.dao.KafkaUserMapper;
import com.xuxd.kafka.console.utils.ConvertUtil;
import com.xuxd.kafka.console.utils.SaslUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import kafka.console.KafkaConfigConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
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

    private final ClusterInfoMapper clusterInfoMapper;

    public KafkaAclSchedule(ObjectProvider<KafkaUserMapper> userMapper,
        ObjectProvider<KafkaConfigConsole> configConsole, ObjectProvider<ClusterInfoMapper> clusterInfoMapper) {
        this.userMapper = userMapper.getIfAvailable();
        this.configConsole = configConsole.getIfAvailable();
        this.clusterInfoMapper = clusterInfoMapper.getIfAvailable();
    }

    @Scheduled(cron = "${cron.clear-dirty-user}")
    public void clearDirtyKafkaUser() {
        try {
            log.info("Start clear dirty data for kafka user from database.");
            List<ClusterInfoDO> clusterInfoDOS = clusterInfoMapper.selectList(null);
            List<Long> clusterInfoIds = new ArrayList<>();
            for (ClusterInfoDO infoDO : clusterInfoDOS) {
                ContextConfig config = new ContextConfig();
                config.setClusterInfoId(infoDO.getId());
                config.setClusterName(infoDO.getClusterName());

                config.setBootstrapServer(infoDO.getAddress());
                if (StringUtils.isNotBlank(infoDO.getProperties())) {
                    config.setProperties(ConvertUtil.toProperties(infoDO.getProperties()));
                }
                ContextConfigHolder.CONTEXT_CONFIG.set(config);
                if (SaslUtil.isEnableSasl() && SaslUtil.isEnableScram()) {
                    log.info("Start clear cluster: {}", infoDO.getClusterName());
                    Set<String> userSet = configConsole.getUserList(null);
                    QueryWrapper<KafkaUserDO> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("cluster_info_id", infoDO.getId());
                    userMapper.selectList(queryWrapper).forEach(u -> {
                        if (!userSet.contains(u.getUsername())) {
                            log.info("clear user: {} from database.", u.getUsername());
                            try {
                                userMapper.deleteById(u.getId());
                            } catch (Exception e) {
                                log.error("userMapper.deleteById error, user: " + u, e);
                            }
                        }
                    });
                    clusterInfoIds.add(infoDO.getId());
                }
            }
            if (CollectionUtils.isNotEmpty(clusterInfoIds)) {
                log.info("Clear the cluster id {}, which not found.", clusterInfoIds);
                QueryWrapper<KafkaUserDO> wrapper = new QueryWrapper<>();
                wrapper.notIn("cluster_info_id", clusterInfoIds);
                userMapper.delete(wrapper);
            }
            log.info("Clear end.");
        } finally {
            ContextConfigHolder.CONTEXT_CONFIG.remove();
        }
    }
}
