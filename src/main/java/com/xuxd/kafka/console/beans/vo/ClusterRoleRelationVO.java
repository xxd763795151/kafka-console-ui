package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.ClusterRoleRelationDO;
import lombok.Data;

/**
 * @author: xuxd
 * @since: 2023/8/23 21:45
 **/
@Data
public class ClusterRoleRelationVO {

    private Long id;

    private Long roleId;

    private Long clusterInfoId;

    private String updateTime;

    private String roleName;

    private String clusterName;

    public static ClusterRoleRelationVO from(ClusterRoleRelationDO relationDO) {
        ClusterRoleRelationVO vo = new ClusterRoleRelationVO();
        vo.setId(relationDO.getId());
        vo.setRoleId(relationDO.getRoleId());
        vo.setClusterInfoId(relationDO.getClusterInfoId());
        vo.setUpdateTime(relationDO.getUpdateTime());
        return vo;
    }
}
