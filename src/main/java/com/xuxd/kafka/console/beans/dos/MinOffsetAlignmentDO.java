package com.xuxd.kafka.console.beans.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-26 10:32:05
 **/
@Data
@TableName("t_min_offset_alignment")
public class MinOffsetAlignmentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String groupId;

    private String topic;

    private String thatOffset;

    private String thisOffset;

    private String updateTime;
}
