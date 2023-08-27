package com.xuxd.kafka.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.beans.dos.ClusterRoleRelationDO;
import com.xuxd.kafka.console.beans.dos.SysRoleDO;
import com.xuxd.kafka.console.beans.dto.ClusterRoleRelationDTO;
import com.xuxd.kafka.console.beans.vo.ClusterRoleRelationVO;
import com.xuxd.kafka.console.dao.ClusterInfoMapper;
import com.xuxd.kafka.console.dao.ClusterRoleRelationMapper;
import com.xuxd.kafka.console.dao.SysRoleMapper;
import com.xuxd.kafka.console.service.ClusterRoleRelationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author: xuxd
 * @since: 2023/8/23 21:50
 **/
@Slf4j
@Service
public class ClusterRoleRelationServiceImpl implements ClusterRoleRelationService {

    private final ClusterRoleRelationMapper mapper;

    private final SysRoleMapper roleMapper;

    private final ClusterInfoMapper clusterInfoMapper;

    public ClusterRoleRelationServiceImpl(final ClusterRoleRelationMapper mapper,
                                          final SysRoleMapper roleMapper,
                                          final ClusterInfoMapper clusterInfoMapper) {
        this.mapper = mapper;
        this.roleMapper = roleMapper;
        this.clusterInfoMapper = clusterInfoMapper;
    }

    @Override
    public ResponseData select() {
        List<ClusterRoleRelationDO> dos = mapper.selectList(null);

        Map<Long, SysRoleDO> roleMap = roleMapper.selectList(null).stream().
                collect(Collectors.toMap(SysRoleDO::getId, Function.identity(), (e1, e2) -> e2));
        Map<Long, ClusterInfoDO> clusterMap = clusterInfoMapper.selectList(null).stream().
                collect(Collectors.toMap(ClusterInfoDO::getId, Function.identity(), (e1, e2) -> e2));
        List<ClusterRoleRelationVO> vos = dos.stream().
                map(aDo -> {
                    ClusterRoleRelationVO vo = ClusterRoleRelationVO.from(aDo);
                    if (roleMap.containsKey(vo.getRoleId())) {
                        vo.setRoleName(roleMap.get(vo.getRoleId()).getRoleName());
                    }
                    if (clusterMap.containsKey(vo.getClusterInfoId())) {
                        vo.setClusterName(clusterMap.get(vo.getClusterInfoId()).getClusterName());
                    }
                    return vo;
                }).collect(Collectors.toList());
        return ResponseData.create().data(vos).success();
    }

    @Override
    public ResponseData add(ClusterRoleRelationDTO dto) {
        ClusterRoleRelationDO relationDO = dto.toDO();
        if (relationDO.getClusterInfoId() == -1L) {
            // all insert
            for (ClusterInfoDO clusterInfoDO : clusterInfoMapper.selectList(null)) {
                ClusterRoleRelationDO aDo = new ClusterRoleRelationDO();
                aDo.setRoleId(relationDO.getRoleId());
                aDo.setClusterInfoId(clusterInfoDO.getId());
                insertIfNotExist(aDo);
            }
        } else {
            insertIfNotExist(relationDO);
        }
        return ResponseData.create().success();
    }

    @Override
    public ResponseData delete(Long id) {
        mapper.deleteById(id);
        return ResponseData.create().success();
    }

    private void insertIfNotExist(ClusterRoleRelationDO relationDO) {
        QueryWrapper<ClusterRoleRelationDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("role_id", relationDO.getRoleId()).
                eq("cluster_info_id", relationDO.getClusterInfoId());
        Integer count = mapper.selectCount(queryWrapper);
        if (count > 0) {
            log.info("已存在，不再增加：{}", relationDO);
            return;
        }
        mapper.insert(relationDO);
    }
}
