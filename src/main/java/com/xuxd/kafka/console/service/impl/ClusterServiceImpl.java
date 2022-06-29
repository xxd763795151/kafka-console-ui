package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.BrokerNode;
import com.xuxd.kafka.console.beans.ClusterInfo;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.vo.BrokerApiVersionVO;
import com.xuxd.kafka.console.beans.vo.ClusterInfoVO;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.service.ClusterService;

import java.util.*;
import java.util.stream.Collectors;
import kafka.console.ClusterConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.NodeApiVersions;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:23:09
 **/
@Slf4j
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
        ClusterInfo clusterInfo = clusterConsole.clusterInfo();
        Set<BrokerNode> nodes = clusterInfo.getNodes();
        if (nodes == null) {
            log.error("集群节点信息为空，集群地址可能不正确或集群内没有活跃节点");
            return ResponseData.create().failed("集群节点信息为空，集群地址可能不正确或集群内没有活跃节点");
        }
        clusterInfo.setNodes(new TreeSet<>(nodes));
        return ResponseData.create().data(clusterInfo).success();
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

    @Override public ResponseData getBrokerApiVersionInfo() {
        HashMap<Node, NodeApiVersions> map = clusterConsole.listBrokerVersionInfo();
        List<BrokerApiVersionVO> list = new ArrayList<>(map.size());
        map.forEach(((node, versions) -> {
            BrokerApiVersionVO vo = new BrokerApiVersionVO();
            vo.setBrokerId(node.id());
            vo.setHost(node.host() + ":" + node.port());
            vo.setSupportNums(versions.allSupportedApiVersions().size());
            String versionInfo = versions.toString(true);
            int from = 0;
            int count = 0;
            int index = -1;
            while ((index = versionInfo.indexOf("UNSUPPORTED", from)) >= 0 && from < versionInfo.length()) {
                count++;
                from = index + 1;
            }
            vo.setUnSupportNums(count);
            versionInfo = versionInfo.substring(1, versionInfo.length() - 2);
            vo.setVersionInfo(Arrays.asList(StringUtils.split(versionInfo, ",")));
            list.add(vo);
        }));
        Collections.sort(list, Comparator.comparingInt(BrokerApiVersionVO::getBrokerId));
        return ResponseData.create().data(list).success();
    }

}
