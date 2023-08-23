package com.xuxd.kafka.console.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xuxd.kafka.console.beans.dos.ClusterRoleRelationDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Cluster info and role relation.
 *
 * @author: xuxd
 * @since: 2023/8/23 21:40
 **/
@Mapper
public interface ClusterRoleRelationMapper extends BaseMapper<ClusterRoleRelationDO> {
}
