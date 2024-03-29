package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.ControllerLog;
import com.xuxd.kafka.console.aspect.annotation.Permission;
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

    @Permission("acl:sasl-scram")
    @GetMapping
    public Object getUserList() {
        return aclService.getUserList();
    }

    @ControllerLog("增加SaslUser")
    @Permission({"acl:sasl-scram:add-update", "acl:sasl-scram:add-auth"})
    @PostMapping
    public Object addOrUpdateUser(@RequestBody AclUser user) {
        return aclService.addOrUpdateUser(user.getUsername(), user.getPassword());
    }

    @ControllerLog("删除SaslUser")
    @Permission({"acl:sasl-scram:del", "acl:sasl-scram:pure"})
    @DeleteMapping
    public Object deleteUser(@RequestBody AclUser user) {
        return aclService.deleteUser(user.getUsername());
    }


    @ControllerLog("删除SaslUser和Acl")
    @Permission({"acl:sasl-scram:del", "acl:sasl-scram:pure"})
    @DeleteMapping("/auth")
    public Object deleteUserAndAuth(@RequestBody AclUser user) {
        return aclService.deleteUserAndAuth(user.getUsername());
    }

    @Permission("acl:sasl-scram:detail")
    @GetMapping("/detail")
    public Object getUserDetail(@RequestParam("username") String username) {
        return aclService.getUserDetail(username);
    }

    @GetMapping("/scram")
    public Object getSaslScramUserList(@RequestParam(required = false, name = "username") String username) {
        AclEntry entry = new AclEntry();
        entry.setPrincipal(StringUtils.isNotBlank(username) ? username : null);
        return aclService.getSaslScramUserList(entry);
    }
}
