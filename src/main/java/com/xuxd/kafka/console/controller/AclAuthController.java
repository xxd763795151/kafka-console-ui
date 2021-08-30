package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.dto.ConsumerAuthDTO;
import com.xuxd.kafka.console.beans.dto.DeleteAclDTO;
import com.xuxd.kafka.console.beans.dto.ProducerAuthDTO;
import com.xuxd.kafka.console.service.AclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:47:48
 **/
@RestController
@RequestMapping("/acl")
public class AclAuthController {

    @Autowired
    private AclService aclService;

    @GetMapping
    public Object getAclList() {
        return aclService.getAclList();
    }

    @PostMapping
    public Object addAcl(@RequestBody AclEntry entry) {
        return aclService.addAcl(entry);
    }

    /**
     * add producer acl.
     *
     * @param param entry.topic && entry.username must.
     * @return
     */
    @PostMapping("/producer")
    public Object addProducerAcl(@RequestBody ProducerAuthDTO param) {

        return aclService.addProducerAcl(param.toEntry());
    }

    /**
     * add consumer acl.
     *
     * @param param entry.topic && entry.groupId entry.username must.
     * @return
     */
    @PostMapping("/consumer")
    public Object addConsumerAcl(@RequestBody ConsumerAuthDTO param) {

        return aclService.addConsumerAcl(param.toTopicEntry(), param.toGroupEntry());
    }

    /**
     * delete user acl .
     *
     * @param param entry.username
     * @return
     */
    @DeleteMapping("/user")
    public Object deleteAclByUser(@RequestBody DeleteAclDTO param) {
        return aclService.deleteUserAcl(param.toUserEntry());
    }

    /**
     * add producer acl.
     *
     * @param param entry.topic && entry.username must.
     * @return
     */
    @DeleteMapping("/producer")
    public Object deleteProducerAcl(@RequestBody ProducerAuthDTO param) {

        return aclService.deleteProducerAcl(param.toEntry());
    }

    /**
     * add consumer acl.
     *
     * @param param entry.topic && entry.groupId entry.username must.
     * @return
     */
    @DeleteMapping("/consumer")
    public Object deleteConsumerAcl(@RequestBody ConsumerAuthDTO param) {

        return aclService.deleteConsumerAcl(param.toTopicEntry(), param.toGroupEntry());
    }

}
