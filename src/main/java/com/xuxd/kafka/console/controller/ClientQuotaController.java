package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.dto.QueryClientQuotaDTO;
import com.xuxd.kafka.console.service.ClientQuotaService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
