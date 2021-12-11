package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.dto.QueryMessageDTO;
import com.xuxd.kafka.console.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:22:19
 **/
@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/search/time")
    public Object searchByTime(@RequestBody QueryMessageDTO dto) {
        return messageService.searchByTime(dto.toQueryMessage());
    }
}
