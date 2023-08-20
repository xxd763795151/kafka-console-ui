package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.QueryMessage;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.beans.SendMessage;
import com.xuxd.kafka.console.beans.dto.QueryMessageDTO;
import com.xuxd.kafka.console.service.MessageService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @Permission("message:search-time")
    @PostMapping("/search/time")
    public Object searchByTime(@RequestBody QueryMessageDTO dto) {
        return messageService.searchByTime(dto.toQueryMessage());
    }

    @Permission("message:search-offset")
    @PostMapping("/search/offset")
    public Object searchByOffset(@RequestBody QueryMessageDTO dto) {
        return messageService.searchByOffset(dto.toQueryMessage());
    }

    @Permission("message:detail")
    @PostMapping("/search/detail")
    public Object searchDetail(@RequestBody QueryMessageDTO dto) {
        return messageService.searchDetail(dto.toQueryMessage());
    }

    @GetMapping("/deserializer/list")
    public Object deserializerList() {
        return messageService.deserializerList();
    }

    @ControllerLog("在线发送消息")
    @Permission("message:send")
    @PostMapping("/send")
    public Object send(@RequestBody SendMessage message) {
        return messageService.send(message);
    }

    @ControllerLog("重新发送消息")
    @Permission("message:resend")
    @PostMapping("/resend")
    public Object resend(@RequestBody SendMessage message) {
        return messageService.resend(message);
    }

    @ControllerLog("在线删除消息")
    @Permission("message:del")
    @DeleteMapping
    public Object delete(@RequestBody List<QueryMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return ResponseData.create().failed("params is null");
        }
        return messageService.delete(messages);
    }
}
