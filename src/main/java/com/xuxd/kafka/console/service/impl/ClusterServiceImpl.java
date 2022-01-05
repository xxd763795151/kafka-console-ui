package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.vo.ClusterInfoVO;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.service.ClusterService;
import java.util.List;
import java.util.stream.Collectors;
import kafka.console.ClusterConsole;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:23:09
 **/
@Service
public class ClusterServiceImpl implements ClusterService {

    private final ClusterConsole clusterConsole;

    private final ClusterInfoMapper clusterInfoMapper;

    public ClusterServiceImpl(ObjectProvider<ClusterConsole> clusterConsole,
        ObjectProvider<ClusterInfoMapper> clusterInfoMapper) {
        this.clusterConsole = clusterConsole.getIfAvailable();
        this.clusterInfoMapper = clusterInfoMapper.getIfAvailable();
    }

    @Override public ResponseData getClusterInfo() {
        return ResponseData.create().data(clusterConsole.clusterInfo()).success();
    }

    @Override public ResponseData getClusterInfoList() {
        return ResponseData.create().data(clusterInfoMapper.selectList(null)
            .stream().map(ClusterInfoVO::from).collect(Collectors.toList())).success();
    }

    @Override public ResponseData addClusterInfo(ClusterInfoDO infoDO) {
        QueryWrapper<ClusterInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_name", infoDO.getClusterName());
        if (clusterInfoMapper.selectCount(queryWrapper) > 0) {
            return ResponseData.create().failed("cluster name exist.");
        }
        clusterInfoMapper.insert(infoDO);
        return ResponseData.create().success();
    }

    @Override public ResponseData deleteClusterInfo(Long id) {
        clusterInfoMapper.deleteById(id);
        return ResponseData.create().success();
    }

    @Override public ResponseData updateClusterInfo(ClusterInfoDO infoDO) {
        clusterInfoMapper.updateById(infoDO);
        return ResponseData.create().success();
    }

    @Override public ResponseData peekClusterInfo() {
        List<ClusterInfoDO> dos = clusterInfoMapper.selectList(null);
        if (CollectionUtils.isEmpty(dos)) {
            return ResponseData.create().failed("No Cluster Info.");
        }
        return ResponseData.create().data(dos.stream().findFirst().map(ClusterInfoVO::from)).success();
    }

}
