package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.SendMessage;
import com.xuxd.kafka.console.beans.dto.QueryMessageDTO;
import com.xuxd.kafka.console.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/search/offset")
    public Object searchByOffset(@RequestBody QueryMessageDTO dto) {
        return messageService.searchByOffset(dto.toQueryMessage());
    }

    @PostMapping("/search/detail")
    public Object searchDetail(@RequestBody QueryMessageDTO dto) {
        return messageService.searchDetail(dto.toQueryMessage());
    }

    @GetMapping("/deserializer/list")
    public Object deserializerList() {
        return messageService.deserializerList();
    }

    @PostMapping("/send")
    public Object send(@RequestBody SendMessage message) {
        return messageService.send(message);
    }

    @PostMapping("/resend")
    public Object resend(@RequestBody SendMessage message) {
        return messageService.resend(message);
    }
}
