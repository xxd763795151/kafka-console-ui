package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.service.ClusterService;
import kafka.console.ClusterConsole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:23:09
 **/
@Service
public class ClusterServiceImpl implements ClusterService {

    @Autowired
    private ClusterConsole clusterConsole;

    @Override public ResponseData getClusterInfo() {
        return ResponseData.create().data(clusterConsole.clusterInfo()).success();
    }
}
