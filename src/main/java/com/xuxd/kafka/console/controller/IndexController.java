package com.xuxd.kafka.console.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 10:54:58
 **/
@RestController
public class IndexController {

    @RequestMapping("/")
    public String index() {
        return "hello world";
    }
}
