package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.dto.AlterClientQuotaDTO;
import com.xuxd.kafka.console.beans.dto.QueryClientQuotaDTO;
import com.xuxd.kafka.console.service.ClientQuotaService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.web.bind.annotation.*;

/**
 * @author: xuxd
 * @date: 2023/1/9 21:50
 **/
@RestController
@RequestMapping("/client/quota")
public class ClientQuotaController {

    private final ClientQuotaService clientQuotaService;

    public ClientQuotaController(ClientQuotaService clientQuotaService) {
        this.clientQuotaService = clientQuotaService;
    }

    @PostMapping("/list")
    public Object getClientQuotaConfigs(@RequestBody QueryClientQuotaDTO request) {
        return clientQuotaService.getClientQuotaConfigs(request.getTypes(), request.getNames());
    }

    @PostMapping
    public Object alterClientQuotaConfigs(@RequestBody AlterClientQuotaDTO request) {
        if (CollectionUtils.isEmpty(request.getTypes())
                || CollectionUtils.isEmpty(request.getNames())
                || request.getTypes().size() != request.getNames().size()) {
            return ResponseData.create().failed("types length and names length is invalid.");
        }
        return clientQuotaService.alterClientQuotaConfigs(request);
    }

    @DeleteMapping
    public Object deleteClientQuotaConfigs(@RequestBody AlterClientQuotaDTO request) {
        if (CollectionUtils.isEmpty(request.getTypes())
                || CollectionUtils.isEmpty(request.getNames())
                || request.getTypes().size() != request.getNames().size()) {
            return ResponseData.create().failed("types length and names length is invalid.");
        }
        return clientQuotaService.deleteClientQuotaConfigs(request);
    }
}
