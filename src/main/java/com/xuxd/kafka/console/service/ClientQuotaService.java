package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AlterClientQuotaDTO;

import java.util.List;

/**
 * @author 晓东哥哥
 */
public interface ClientQuotaService {

    ResponseData getClientQuotaConfigs(List<String> types, List<String> names);

    ResponseData alterClientQuotaConfigs(AlterClientQuotaDTO request);

    ResponseData deleteClientQuotaConfigs(AlterClientQuotaDTO request);
}
