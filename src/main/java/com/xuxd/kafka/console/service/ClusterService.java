package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:22:30
 **/
public interface ClusterService {
    ResponseData getClusterInfo();
}
