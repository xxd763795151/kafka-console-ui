package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.kafka.common.config.internals.QuotaConfigs;
import org.apache.kafka.common.quota.ClientQuotaEntity;

import java.util.List;
import java.util.Map;

/**
 * @author 晓东哥哥
 */
@Data
public class ClientQuotaEntityVO {

    private String user;

    private String client;

    private String ip;

    private String consumerRate;

    private String producerRate;

    private String requestPercentage;

    public static ClientQuotaEntityVO from(ClientQuotaEntity entity, List<String> entityTypes, Map<String, Object> config) {
        ClientQuotaEntityVO entityVO = new ClientQuotaEntityVO();
        Map<String, String> entries = entity.entries();
        entityTypes.forEach(type -> {
            switch (type) {
                case ClientQuotaEntity.USER:
                    entityVO.setUser(entries.get(type));
                    break;
                case ClientQuotaEntity.CLIENT_ID:
                    entityVO.setClient(entries.get(type));
                    break;
                case ClientQuotaEntity.IP:
                    entityVO.setIp(entries.get(type));
                    break;
                default:
                    break;
            }
        });
        entityVO.setConsumerRate(config.getOrDefault(QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG, "").toString());
        entityVO.setProducerRate(config.getOrDefault(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, "").toString());
        entityVO.setRequestPercentage(config.getOrDefault(QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG, "").toString());

        return entityVO;
    }
}
