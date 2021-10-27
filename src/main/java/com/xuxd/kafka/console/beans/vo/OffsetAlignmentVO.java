package com.xuxd.kafka.console.beans.vo;

import com.google.gson.Gson;
import com.xuxd.kafka.console.beans.dos.MinOffsetAlignmentDO;
import java.util.Map;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-27 19:44:14
 **/
@Data
public class OffsetAlignmentVO {

    private static final Gson GSON = new Gson();

    private Long id;

    private String groupId;

    private String topic;

    private Map<String, Object> thatOffset;

    private Map<String, Object> thisOffset;

    private String updateTime;

    public static OffsetAlignmentVO from(MinOffsetAlignmentDO alignmentDO) {
        OffsetAlignmentVO vo = new OffsetAlignmentVO();
        vo.id = alignmentDO.getId();
        vo.groupId = alignmentDO.getGroupId();
        vo.topic = alignmentDO.getTopic();
        vo.thatOffset = GSON.fromJson(alignmentDO.getThatOffset(), Map.class);
        vo.thisOffset = GSON.fromJson(alignmentDO.getThisOffset(), Map.class);
        vo.updateTime = alignmentDO.getUpdateTime();
        return vo;
    }
}
