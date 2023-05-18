package com.xuxd.kafka.console.controller;

import com.xuxd.kafka.console.aspect.annotation.Permission;
import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.dto.AddAuthDTO;
import com.xuxd.kafka.console.beans.dto.ConsumerAuthDTO;
import com.xuxd.kafka.console.beans.dto.DeleteAclDTO;
import com.xuxd.kafka.console.beans.dto.ProducerAuthDTO;
import com.xuxd.kafka.console.beans.dto.QueryAclDTO;
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

    @Permission({"acl:authority:detail", "acl:sasl-scram:detail"})
    @PostMapping("/detail")
    public Object getAclDetailList(@RequestBody QueryAclDTO param) {
        return aclService.getAclDetailList(param.toEntry());
    }

    @GetMapping("/operation/list")
    public Object getAclOperationList() {
        return aclService.getOperationList();
    }

    @Permission("acl:authority")
    @PostMapping("/list")
    public Object getAclList(@RequestBody QueryAclDTO param) {
        return aclService.getAclList(param.toEntry());
    }

    @Permission({"acl:authority:add-principal", "acl:authority:add", "acl:sasl-scram:add-auth"})
    @PostMapping
    public Object addAcl(@RequestBody AddAuthDTO param) {
        return aclService.addAcl(param.toAclEntry());
    }

    /**
     * add producer acl.
     *
     * @param param entry.topic && entry.username must.
     * @return
     */
    @Permission({"acl:authority:producer", "acl:sasl-scram:producer"})
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
    @Permission({"acl:authority:consumer", "acl:sasl-scram:consumer"})
    @PostMapping("/consumer")
    public Object addConsumerAcl(@RequestBody ConsumerAuthDTO param) {

        return aclService.addConsumerAcl(param.toTopicEntry(), param.toGroupEntry());
    }

    /**
     * delete acl .
     *
     * @param entry entry
     * @return
     */
    @Permission({"acl:authority:clean", "acl:sasl-scram:pure"})
    @DeleteMapping
    public Object deleteAclByUser(@RequestBody AclEntry entry) {
        return aclService.deleteAcl(entry);
    }

    /**
     * delete user acl .
     *
     * @param param entry.username
     * @return
     */
    @Permission({"acl:authority:clean", "acl:sasl-scram:pure"})
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
    @Permission({"acl:authority:clean", "acl:sasl-scram:pure"})
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
    @Permission({"acl:authority:clean", "acl:sasl-scram:pure"})
    @DeleteMapping("/consumer")
    public Object deleteConsumerAcl(@RequestBody ConsumerAuthDTO param) {

        return aclService.deleteConsumerAcl(param.toTopicEntry(), param.toGroupEntry());
    }

    /**
     * clear principal acls.
     *
     * @param param acl principal.
     * @return true or false.
     */
    @Permission({"acl:authority:clean", "acl:sasl-scram:pure"})
    @DeleteMapping("/clear")
    public Object clearAcl(@RequestBody DeleteAclDTO param) {
        return aclService.clearAcl(param.toUserEntry());
    }
}
