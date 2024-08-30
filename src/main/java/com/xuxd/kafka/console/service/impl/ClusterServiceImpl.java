package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.BrokerNode;
import com.xuxd.kafka.console.beans.ClusterInfo;
import com.xuxd.kafka.console.beans.Credentials;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.dos.ClusterRoleRelationDO;
import com.xuxd.kafka.console.beans.vo.BrokerApiVersionVO;
import com.xuxd.kafka.console.beans.vo.ClusterInfoVO;
import com.xuxd.kafka.console.config.AuthConfig;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.dao.ClusterRoleRelationMapper;
import com.xuxd.kafka.console.filter.CredentialsContext;
import com.xuxd.kafka.console.service.ClusterService;
import kafka.console.BrokerVersion;
import kafka.console.ClusterConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.NodeApiVersions;
import org.apache.kafka.common.Node;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    private final AuthConfig authConfig;

    private final ClusterRoleRelationMapper clusterRoleRelationMapper;

    public ClusterServiceImpl(final ObjectProvider<ClusterConsole> clusterConsole,
                              final ObjectProvider<ClusterInfoMapper> clusterInfoMapper,
                              final AuthConfig authConfig,
                              final ClusterRoleRelationMapper clusterRoleRelationMapper) {
        this.clusterConsole = clusterConsole.getIfAvailable();
        this.clusterInfoMapper = clusterInfoMapper.getIfAvailable();
        this.authConfig = authConfig;
        this.clusterRoleRelationMapper = clusterRoleRelationMapper;
    }

    @Override
    public ResponseData getClusterInfo() {
        ClusterInfo clusterInfo = clusterConsole.clusterInfo();
        Set<BrokerNode> nodes = clusterInfo.getNodes();
        if (nodes == null) {
            log.error("集群节点信息为空，集群地址可能不正确或集群内没有活跃节点");
            return ResponseData.create().failed("集群节点信息为空，集群地址可能不正确或集群内没有活跃节点");
        }
        clusterInfo.setNodes(new TreeSet<>(nodes));
        return ResponseData.create().data(clusterInfo).success();
    }

    @Override
    public ResponseData getClusterInfoListForSelect() {
        return ResponseData.create().
                data(clusterInfoMapper.selectList(null).stream().
                        map(e -> {
                            ClusterInfoVO vo = ClusterInfoVO.from(e);
                            vo.setProperties(Collections.emptyList());
                            vo.setAddress("");
                            return vo;
                        }).collect(Collectors.toList())).success();
    }

    @Override
    public ResponseData getClusterInfoList() {
        // 如果开启权限管理，当前用户没有集群切换->集群信息的编辑权限，隐藏集群的属性信息，避免ACL属性暴露出来
        Credentials credentials = CredentialsContext.get();
        boolean enableClusterAuthority = credentials != null && authConfig.isEnableClusterAuthority();
        final Set<Long> clusterInfoIdSet = new HashSet<>();
        if (enableClusterAuthority) {
            List<Long> roleIdList = credentials.getRoleIdList();
            QueryWrapper<ClusterRoleRelationDO> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("role_id", roleIdList);
            clusterInfoIdSet.addAll(clusterRoleRelationMapper.selectList(queryWrapper).
                    stream().map(ClusterRoleRelationDO::getClusterInfoId).
                    collect(Collectors.toSet()));
        }
        return ResponseData.create().
                data(clusterInfoMapper.selectList(null).stream().
                        filter(e -> !enableClusterAuthority || clusterInfoIdSet.contains(e.getId())).
                        map(e -> {
                            ClusterInfoVO vo = ClusterInfoVO.from(e);
                            if (credentials != null && credentials.isHideClusterProperty()) {
                                vo.setProperties(Collections.emptyList());
                            }
                            return vo;
                        }).collect(Collectors.toList())).success();
    }

    @Override
    public ResponseData addClusterInfo(ClusterInfoDO infoDO) {
        QueryWrapper<ClusterInfoDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("cluster_name", infoDO.getClusterName());
        if (clusterInfoMapper.selectCount(queryWrapper) > 0) {
            return ResponseData.create().failed("cluster name exist.");
        }
        clusterInfoMapper.insert(infoDO);
        Credentials credentials = CredentialsContext.get();
        boolean enableClusterAuthority = credentials != null && authConfig.isEnableClusterAuthority();
        if (enableClusterAuthority) {
            for (Long roleId : credentials.getRoleIdList()) {
                // 开启集群的数据权限控制，新增集群的时候必须要录入一条信息
                QueryWrapper<ClusterRoleRelationDO> relationQueryWrapper = new QueryWrapper<>();
                relationQueryWrapper.eq("role_id", roleId).
                        eq("cluster_info_id", infoDO.getId());
                Integer count = clusterRoleRelationMapper.selectCount(relationQueryWrapper);
                if (count <= 0) {
                    ClusterRoleRelationDO relationDO = new ClusterRoleRelationDO();
                    relationDO.setRoleId(roleId);
                    relationDO.setClusterInfoId(infoDO.getId());
                    clusterRoleRelationMapper.insert(relationDO);
                }
            }
        }
        return ResponseData.create().success();
    }

    @Override
    public ResponseData deleteClusterInfo(Long id) {
        clusterInfoMapper.deleteById(id);
        Credentials credentials = CredentialsContext.get();
        boolean enableClusterAuthority = credentials != null && authConfig.isEnableClusterAuthority();
        if (enableClusterAuthority) {
            for (Long roleId : credentials.getRoleIdList()) {
                // 开启集群的数据权限控制，删除集群的时候必须要删除对应的数据权限
                QueryWrapper<ClusterRoleRelationDO> relationQueryWrapper = new QueryWrapper<>();
                relationQueryWrapper.eq("role_id", roleId).eq("cluster_info_id", id);
                clusterRoleRelationMapper.delete(relationQueryWrapper);
            }
        }
        return ResponseData.create().success();
    }

    @Override
    public ResponseData updateClusterInfo(ClusterInfoDO infoDO) {
        if (infoDO.getProperties() == null) {
            // null 的话不更新，这个是bug，设置为空字符串解决
            infoDO.setProperties("");
        }
        clusterInfoMapper.updateById(infoDO);
        return ResponseData.create().success();
    }

    @Override
    public ResponseData peekClusterInfo() {
        List<ClusterInfoDO> dos = clusterInfoMapper.selectList(null);
        if (CollectionUtils.isEmpty(dos)) {
            return ResponseData.create().failed("No Cluster Info.");
        }
        return ResponseData.create().data(dos.stream().findFirst().map(ClusterInfoVO::from)).success();
    }

    @Override
    public ResponseData getBrokerApiVersionInfo() {
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
            // 推测broker版本
            String vs = BrokerVersion.guessBrokerVersion(versions);
            vo.setBrokerVersion(vs);
        }));
        Collections.sort(list, Comparator.comparingInt(BrokerApiVersionVO::getBrokerId));
        return ResponseData.create().data(list).success();
    }

}
