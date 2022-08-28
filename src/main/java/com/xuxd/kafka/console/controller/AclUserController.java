package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.AclUser;
import com.xuxd.kafka.console.service.AclService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui. sasl scram user.
 *
 * @author xuxd
 * @date 2021-08-28 21:13:05
 **/
@RestController
@RequestMapping("/user")
public class AclUserController {

    @Autowired
    private AclService aclService;

    @GetMapping
    public Object getUserList() {
        return aclService.getUserList();
    }

    @PostMapping
    public Object addOrUpdateUser(@RequestBody AclUser user) {
        return aclService.addOrUpdateUser(user.getUsername(), user.getPassword());
    }

    @DeleteMapping
    public Object deleteUser(@RequestBody AclUser user) {
        return aclService.deleteUser(user.getUsername());
    }


    @DeleteMapping("/auth")
    public Object deleteUserAndAuth(@RequestBody AclUser user) {
        return aclService.deleteUserAndAuth(user.getUsername());
    }

    @GetMapping("/detail")
    public Object getUserDetail(@RequestParam String username) {
        return aclService.getUserDetail(username);
    }

    @GetMapping("/scram")
    public Object getSaslScramUserList(@RequestParam(required = false) String username) {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(StringUtils.isNotBlank(username) ? username : null);
        return aclService.getSaslScramUserList(entry);
    }
}
