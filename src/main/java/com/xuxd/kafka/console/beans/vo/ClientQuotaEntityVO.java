package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.kafka.common.config.internals.QuotaConfigs;
import org.apache.kafka.common.quota.ClientQuotaEntity;

import java.util.Map;

@Data
public class ClientQuotaEntityVO {

    private String entry;

    private String consumerRate;

    private String producerRate;

    private String requestPercentage;

    public static ClientQuotaEntityVO from(ClientQuotaEntity entity, String type, Map<String, Object> config) {
        ClientQuotaEntityVO entityVO = new ClientQuotaEntityVO();
        entityVO.setEntry(entity.entries().get(type));
        entityVO.setConsumerRate(config.getOrDefault(QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG, "").toString());
        entityVO.setProducerRate(config.getOrDefault(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, "").toString());
        entityVO.setRequestPercentage(config.getOrDefault(QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG, "").toString());

        return entityVO;
    }
}
