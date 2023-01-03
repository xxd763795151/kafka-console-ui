package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.vo.ClientQuotaEntityVO;

import java.util.List;

public interface ClientQuotaService {

    List<ClientQuotaEntityVO> getClientQuotaConfigs(String type, String name);
}
