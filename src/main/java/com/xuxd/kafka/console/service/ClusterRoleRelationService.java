package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.ClusterRoleRelationDTO;

/**
 * Cluster info and role relation.
 *
 * @author: xuxd
 * @since: 2023/8/23 21:42
 **/
public interface ClusterRoleRelationService {

    ResponseData select();

    ResponseData add(ClusterRoleRelationDTO dto);

    ResponseData delete(Long id);
}
