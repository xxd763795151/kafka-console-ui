package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.vo.ClientQuotaEntityVO;
import com.xuxd.kafka.console.service.ClientQuotaService;
import kafka.console.ClientQuotaConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientQuotaServiceImpl implements ClientQuotaService {

    private final ClientQuotaConsole clientQuotaConsole;

    private final Map<String, String> typeDict = new HashMap<>();

    {
        typeDict.put("user", ClientQuotaEntity.USER);
        typeDict.put("client-id", ClientQuotaEntity.CLIENT_ID);
        typeDict.put("ip", ClientQuotaEntity.IP);
    }

    public ClientQuotaServiceImpl(ClientQuotaConsole clientQuotaConsole) {
        this.clientQuotaConsole = clientQuotaConsole;
    }

    @Override
    public List<ClientQuotaEntityVO> getClientQuotaConfigs(String type, String name) {
        List<String> entityNames = StringUtils.isNotBlank(name) ? Arrays.asList(name) : Collections.emptyList();
        String entityType = typeDict.get(type);
        if (StringUtils.isEmpty(entityType)) {
            throw new IllegalArgumentException("type不正确：" + type);
        }
        Map<ClientQuotaEntity, Map<String, Object>> clientQuotasConfigs = clientQuotaConsole.getClientQuotasConfigs(Arrays.asList(entityType), entityNames);

        return clientQuotasConfigs.entrySet().stream().map(entry -> ClientQuotaEntityVO.from(entry.getKey(), entityType, entry.getValue())).collect(Collectors.toList());
    }
}
