package com.xuxd.kafka.console.beans;

import com.xuxd.kafka.console.beans.dos.*;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * console internal config data.
 *
 * @author: xuxd
 * @since: 2025/11/20 20:57
 **/
@Data
public class ConsoleData {
    private List<ClusterInfoDO> clusterInfoList = Collections.emptyList();

    private List<ClusterRoleRelationDO> clusterRoleRelationList = Collections.emptyList();

    private List<SysRoleDO> sysRoleList = Collections.emptyList();

    private List<SysUserDO> sysUserList = Collections.emptyList();

    private List<KafkaUserDO> kafkaUserList = Collections.emptyList();
}
