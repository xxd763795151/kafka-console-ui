package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:22:30
 **/
public interface ClusterService {
    ResponseData getClusterInfo();

    ResponseData getClusterInfoList();

    ResponseData addClusterInfo(ClusterInfoDO infoDO);

    ResponseData deleteClusterInfo(Long id);

    ResponseData updateClusterInfo(ClusterInfoDO infoDO);

    ResponseData peekClusterInfo();

    ResponseData getBrokerApiVersionInfo();
}
