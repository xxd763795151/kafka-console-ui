package com.xuxd.kafka.console.beans.vo;

import com.xuxd.kafka.console.beans.dos.ClusterInfoDO;
import com.xuxd.kafka.console.utils.ConvertUtil;
import java.util.List;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2022-01-04 19:16:11
 **/
@Data
public class ClusterInfoVO {

    private Long id;

    private String clusterName;

    private String address;

    private List<String> properties;

    private String updateTime;

    public static ClusterInfoVO from(ClusterInfoDO infoDO) {
        ClusterInfoVO vo = new ClusterInfoVO();
        vo.setId(infoDO.getId());
        vo.setClusterName(infoDO.getClusterName());
        vo.setAddress(infoDO.getAddress());
        vo.setUpdateTime(infoDO.getUpdateTime());
        if (StringUtils.isNotBlank(infoDO.getProperties())) {
            vo.setProperties(ConvertUtil.jsonStr2List(infoDO.getProperties()));
        }
        return vo;

    }
}
