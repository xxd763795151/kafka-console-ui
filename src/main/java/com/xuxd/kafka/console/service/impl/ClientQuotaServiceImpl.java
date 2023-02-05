package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AlterClientQuotaDTO;
import com.xuxd.kafka.console.beans.vo.ClientQuotaEntityVO;
import com.xuxd.kafka.console.service.ClientQuotaService;
import kafka.console.ClientQuotaConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.config.internals.QuotaConfigs;
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

    private final Map<String, String> configDict = new HashMap<>();

    private final String USER = "user";
    private final String CLIENT_ID = "client-id";
    private final String IP = "ip";
    private final String USER_CLIENT = "user&client-id";

    {
        typeDict.put(USER, ClientQuotaEntity.USER);
        typeDict.put(CLIENT_ID, ClientQuotaEntity.CLIENT_ID);
        typeDict.put(IP, ClientQuotaEntity.IP);
        typeDict.put(USER_CLIENT, USER_CLIENT);

        configDict.put("producerRate", QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG);
        configDict.put("consumerRate", QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG);
        configDict.put("requestPercentage", QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG);
    }

    public ClientQuotaServiceImpl(ClientQuotaConsole clientQuotaConsole) {
        this.clientQuotaConsole = clientQuotaConsole;
    }

    @Override
    public ResponseData getClientQuotaConfigs(List<String> types, List<String> names) {
        List<String> entityNames = names == null ? Collections.emptyList() : new ArrayList<>(names);
        List<String> entityTypes = types.stream().map(e -> typeDict.get(e)).filter(e -> e != null).collect(Collectors.toList());
        if (entityTypes.isEmpty() || entityTypes.size() != types.size()) {
            throw new IllegalArgumentException("types illegal.");
        }

        boolean userAndClientFilterClientOnly = false;
        // only type: [user and client-id], type.size == 2
        if (entityTypes.size() == 2) {
            if (names.size() == 2 && StringUtils.isBlank(names.get(0)) && StringUtils.isNotBlank(names.get(1))) {
                userAndClientFilterClientOnly = true;
            }
        }
        Map<ClientQuotaEntity, Map<String, Object>> clientQuotasConfigs = clientQuotaConsole.getClientQuotasConfigs(entityTypes,
                userAndClientFilterClientOnly ? Collections.emptyList() : entityNames);

        List<ClientQuotaEntityVO> voList = clientQuotasConfigs.entrySet().stream().map(entry -> ClientQuotaEntityVO.from(
                entry.getKey(), entityTypes, entry.getValue())).collect(Collectors.toList());
        if (!userAndClientFilterClientOnly) {
            return ResponseData.create().data(voList).success();
        }
        List<ClientQuotaEntityVO> list = voList.stream().filter(e -> names.get(1).equals(e.getClient())).collect(Collectors.toList());

        return ResponseData.create().data(list).success();
    }

    @Override
    public ResponseData alterClientQuotaConfigs(AlterClientQuotaDTO request) {

        if (StringUtils.isEmpty(request.getType()) || !typeDict.containsKey(request.getType())) {
            return ResponseData.create().failed("Unknown type.");
        }

        List<String> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        parseTypesAndNames(request, types, names, request.getType());
        Map<String, String> configsToBeAddedMap = new HashMap<>();

        if (StringUtils.isNotEmpty(request.getProducerRate())) {
            configsToBeAddedMap.put(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG, String.valueOf(Math.floor(Double.valueOf(request.getProducerRate()))));
        }
        if (StringUtils.isNotEmpty(request.getConsumerRate())) {
            configsToBeAddedMap.put(QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG, String.valueOf(Math.floor(Double.valueOf(request.getConsumerRate()))));
        }
        if (StringUtils.isNotEmpty(request.getRequestPercentage())) {
            configsToBeAddedMap.put(QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG, String.valueOf(Math.floor(Double.valueOf(request.getRequestPercentage()))));
        }

        clientQuotaConsole.addQuotaConfigs(types, names, configsToBeAddedMap);
        if (CollectionUtils.isNotEmpty(request.getDeleteConfigs())) {
            List<String> delete = request.getDeleteConfigs().stream().map(key -> configDict.get(key)).collect(Collectors.toList());
            clientQuotaConsole.deleteQuotaConfigs(types, names, delete);
        }
        return ResponseData.create().success();
    }

    @Override
    public ResponseData deleteClientQuotaConfigs(AlterClientQuotaDTO request) {
        if (StringUtils.isEmpty(request.getType()) || !typeDict.containsKey(request.getType())) {
            return ResponseData.create().failed("Unknown type.");
        }
        List<String> types = new ArrayList<>();
        List<String> names = new ArrayList<>();
        parseTypesAndNames(request, types, names, request.getType());
        List<String> configs = new ArrayList<>();
        configs.add(QuotaConfigs.PRODUCER_BYTE_RATE_OVERRIDE_CONFIG);
        configs.add(QuotaConfigs.CONSUMER_BYTE_RATE_OVERRIDE_CONFIG);
        configs.add(QuotaConfigs.REQUEST_PERCENTAGE_OVERRIDE_CONFIG);
        clientQuotaConsole.deleteQuotaConfigs(types, names, configs);
        return ResponseData.create().success();
    }

    private void parseTypesAndNames(AlterClientQuotaDTO request, List<String> types, List<String> names, String type) {
        switch (request.getType()) {
            case USER:
                getTypesAndNames(request, types, names, USER);
                break;
            case CLIENT_ID:
                getTypesAndNames(request, types, names, CLIENT_ID);
                break;
            case IP:
                getTypesAndNames(request, types, names, IP);
                break;
            case USER_CLIENT:
                getTypesAndNames(request, types, names, USER);
                getTypesAndNames(request, types, names, CLIENT_ID);
                break;
        }
    }

    private void getTypesAndNames(AlterClientQuotaDTO request, List<String> types, List<String> names, String type) {
        int index = -1;
        for (int i = 0; i < request.getTypes().size(); i++) {
            if (type.equals(request.getTypes().get(i))) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            throw new IllegalArgumentException("Does not contain the type：" + type);
        }
        types.add(request.getTypes().get(index));
        if (CollectionUtils.isNotEmpty(request.getNames()) && request.getNames().size() > index) {
            names.add(request.getNames().get(index));
        } else {
            names.add("");
        }
    }

}
