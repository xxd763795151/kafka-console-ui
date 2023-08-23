package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.dos.ClusterRoleRelationDO;
import lombok.Data;

/**
 * @author: xuxd
 * @since: 2023/8/23 21:42
 **/
@Data
public class ClusterRoleRelationDTO {

    private Long id;

    private Long roleId;

    private Long clusterInfoId;

    private String updateTime;

    public ClusterRoleRelationDO toDO() {
        ClusterRoleRelationDO aDo = new ClusterRoleRelationDO();
        aDo.setId(id);
        aDo.setRoleId(roleId);
        aDo.setClusterInfoId(clusterInfoId);
        return aDo;
    }
}
