package com.xuxd.kafka.console.beans.dto;

import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.utils.ConvertUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-04 20:19:03
 **/
@Data
public class ClusterInfoDTO {
    private Long id;

    private String clusterName;

    private String address;

    private String properties;

    private String updateTime;

    public ClusterInfoDO to() {
        ClusterInfoDO infoDO = new ClusterInfoDO();
        infoDO.setId(id);
        infoDO.setClusterName(clusterName);
        infoDO.setAddress(address);

        if (StringUtils.isNotBlank(properties)) {
            infoDO.setProperties(ConvertUtil.propertiesStr2JsonStr(properties));
        }

        return infoDO;
    }
}
