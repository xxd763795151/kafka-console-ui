package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.dto.AlterClientQuotaDTO;
import com.xuxd.kafka.console.beans.vo.ClientQuotaEntityVO;

import java.util.List;

/**
 * @author 晓东哥哥
 */
public interface ClientQuotaService {

    List<ClientQuotaEntityVO> getClientQuotaConfigs(List<String> types, List<String> names);

    Object alterClientQuotaConfigs(AlterClientQuotaDTO request);
}
