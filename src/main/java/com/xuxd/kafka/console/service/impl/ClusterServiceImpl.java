package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.vo.ClusterInfoVO;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.service.ClusterService;
import java.util.stream.Collectors;
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

    @Autowired
    private ClusterInfoMapper clusterInfoMapper;

    @Override public ResponseData getClusterInfo() {
        return ResponseData.create().data(clusterConsole.clusterInfo()).success();
    }

    @Override public ResponseData getClusterInfoList() {
        return ResponseData.create().data(clusterInfoMapper.selectList(null)
            .stream().map(ClusterInfoVO::from).collect(Collectors.toList())).success();
    }

    @Override public ResponseData addClusterInfo(ClusterInfoDO infoDO) {
        clusterInfoMapper.insert(infoDO);
        return ResponseData.create().success();
    }

}
