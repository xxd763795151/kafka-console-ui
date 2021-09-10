package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-10 20:16:07
 **/
@Data
public class ConsumerGroupVO {

    private String groupId;

    private boolean isSimpleConsumerGroup;

    private int members;

    private String partitionAssignor;

    private String state;

    private String coordinator;

    private int authorizedOperations;

    public static ConsumerGroupVO from(ConsumerGroupDescription description) {
        ConsumerGroupVO vo = new ConsumerGroupVO();
        vo.setGroupId(description.groupId());
        vo.setSimpleConsumerGroup(description.isSimpleConsumerGroup());
        vo.setMembers(description.members().size());
        vo.setPartitionAssignor(description.partitionAssignor());
        vo.setState(description.state().name());
        vo.setCoordinator(description.coordinator() != null ? description.coordinator().toString() : "");
        vo.setAuthorizedOperations(description.authorizedOperations() != null ? description.authorizedOperations().size() : 0);

        return vo;
    }
}
