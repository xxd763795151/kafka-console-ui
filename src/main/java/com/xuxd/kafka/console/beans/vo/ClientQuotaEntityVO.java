package com.xuxd.kafka.console.beans.vo;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
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
        entityVO.setConsumerRate(convert(config.getOrDefault(QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG, "")));
        entityVO.setProducerRate(convert(config.getOrDefault(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, "")));
        entityVO.setRequestPercentage(config.getOrDefault(QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG, "").toString());
        return entityVO;
    }


    public static String convert(Object num) {
        if (num == null) {
            return null;
        }

        if (num instanceof String) {
            if ((StringUtils.isBlank((String) num))) {
                return (String) num;
            }
        }

        if (num instanceof Number) {
            Number number = (Number) num;
            double value = number.doubleValue();
            double _1kb = 1024;
            double _1mb = 1024 * _1kb;
            if (value < _1kb) {
                return value + " Byte";
            }
            if (value < _1mb) {
                return String.format("%.1f KB", (value / _1kb));
            }
            if (value >= _1mb) {
                return String.format("%.1f MB", (value / _1mb));
            }
        }
        return String.valueOf(num);
    }
}
