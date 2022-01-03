package com.xuxd.kafka.console.beans.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-31 09:54:24
 **/
@Data
@TableName("t_cluster_info")
public class ClusterInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String clusterName;

    private String address;

    private String properties;

    private String updateTime;
}
