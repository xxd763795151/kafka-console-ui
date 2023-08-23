package com.xuxd.kafka.console.beans.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: xuxd
 * @since: 2023/8/23 21:35
 **/
@Data
@TableName("t_cluster_role_relation")
public class ClusterRoleRelationDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long roleId;

    private Long clusterInfoId;

    private String updateTime;
}
