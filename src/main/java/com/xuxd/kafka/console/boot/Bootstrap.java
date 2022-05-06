package com.xuxd.kafka.console.boot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.dos.DevOpsUserDO;
import com.xuxd.kafka.console.config.KafkaConfig;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.dao.DevOpsUserMapper;
import com.xuxd.kafka.console.utils.ConvertUtil;
import java.util.List;

import com.xuxd.kafka.console.utils.Md5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-30 19:16:50
 **/
@Slf4j
@Component
public class Bootstrap implements SmartInitializingSingleton {

    public static final String DEFAULT_CLUSTER_NAME = "default";

    private final KafkaConfig config;

    private final ClusterInfoMapper clusterInfoMapper;

    public Bootstrap(KafkaConfig config, ObjectProvider<ClusterInfoMapper> clusterInfoMapper) {
        this.config = config;
        this.clusterInfoMapper = clusterInfoMapper.getIfAvailable();
    }

    private void initialize() {
        loadDefaultClusterConfig();
    }

    private void loadDefaultClusterConfig() {
        log.info("load default kafka config.");
        if (StringUtils.isBlank(config.getBootstrapServer())) {
            return;
        }

        QueryWrapper<ClusterInfoDO> clusterInfoDOQueryWrapper = new QueryWrapper<>();
        clusterInfoDOQueryWrapper.eq("cluster_name", DEFAULT_CLUSTER_NAME);
        List<Object> objects = clusterInfoMapper.selectObjs(clusterInfoDOQueryWrapper);
        if (CollectionUtils.isNotEmpty(objects)) {
            log.warn("default kafka cluster config has existed[any of cluster name or address].");
            return;
        }

        ClusterInfoDO infoDO = new ClusterInfoDO();
        infoDO.setClusterName(DEFAULT_CLUSTER_NAME);
        infoDO.setAddress(config.getBootstrapServer().trim());
        infoDO.setProperties(ConvertUtil.toJsonString(config.getProperties()));
        clusterInfoMapper.insert(infoDO);
        log.info("Insert default config: {}", infoDO);
    }

    @Override public void afterSingletonsInstantiated() {
        initialize();
    }
}
