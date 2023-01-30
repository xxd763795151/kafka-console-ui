package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AlterClientQuotaDTO;
import com.xuxd.kafka.console.beans.vo.ClientQuotaEntityVO;
import com.xuxd.kafka.console.service.ClientQuotaService;
import kafka.console.ClientQuotaConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.quota.ClientQuotaEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 晓东哥哥
 */
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
    public List<ClientQuotaEntityVO> getClientQuotaConfigs(List<String> types, List<String> names) {
        List<String> entityNames = names == null ? Collections.emptyList() : new ArrayList<>(names);
        List<String> entityTypes = types.stream().map(e -> typeDict.get(e)).filter(e -> e != null).collect(Collectors.toList());
        if (entityTypes.isEmpty() || entityTypes.size() != types.size()) {
            throw new IllegalArgumentException("types illegal.");
        }

        boolean userWithClientFilterClientOnly = false;
        if (entityTypes.size() == 2) {
            if (names.size() == 2 && StringUtils.isBlank(names.get(0)) && StringUtils.isNotBlank(names.get(1))) {
                userWithClientFilterClientOnly = true;
            }
        }
        Map<ClientQuotaEntity, Map<String, Object>> clientQuotasConfigs = clientQuotaConsole.getClientQuotasConfigs(entityTypes,
                userWithClientFilterClientOnly ? Collections.emptyList() : entityNames);

        List<ClientQuotaEntityVO> voList = clientQuotasConfigs.entrySet().stream().map(entry -> ClientQuotaEntityVO.from(
                entry.getKey(), entityTypes, entry.getValue())).collect(Collectors.toList());
        if (!userWithClientFilterClientOnly) {
            return voList;
        }
        return voList.stream().filter(e -> names.get(1).equals(e.getClient())).collect(Collectors.toList());
    }

    @Override
    public Object alterClientQuotaConfigs(AlterClientQuotaDTO request) {

        return ResponseData.create().failed();
    }

}
