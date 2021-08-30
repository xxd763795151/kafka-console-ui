package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.CounterList;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.service.AclService;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.KafkaAclConsole;
import kafka.console.KafkaConfigConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.acl.AclBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:44:40
 **/
@Slf4j
@Service
public class AclServiceImpl implements AclService {

    @Autowired
    private KafkaConfigConsole configConsole;

    @Autowired
    private KafkaAclConsole aclConsole;

    @Override public ResponseData<Set<String>> getUserList() {
        try {
            return ResponseData.create(Set.class).data(configConsole.getUserList()).success();
        } catch (Exception e) {
            log.error("getUserList error.", e);
            return ResponseData.create().failed();
        }
    }

    @Override public ResponseData addOrUpdateUser(String name, String pass) {
        return configConsole.addOrUpdateUser(name, pass) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData deleteUser(String name) {
        return configConsole.deleteUser(name) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData getAclList() {
        List<AclBinding> aclBindingList = aclConsole.getAclList();

        return ResponseData.create().data(new CounterList<>(aclBindingList.stream().map(x -> AclEntry.valueOf(x)).collect(Collectors.toList()))).success();
    }

    @Override public ResponseData deleteAcl(AclEntry entry) {
        return aclConsole.deleteAcl(entry, false, false, false) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData addAcl(AclEntry entry) {
        return aclConsole.addAcl(Collections.singletonList(entry.toAclBinding())) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData addProducerAcl(AclEntry entry) {
        return aclConsole.addProducerAcl(entry) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData addConsumerAcl(AclEntry topic, AclEntry group) {
        return aclConsole.addConsumerAcl(topic, group) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData deleteProducerAcl(AclEntry entry) {
        return aclConsole.deleteProducerAcl(entry) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData deleteConsumerAcl(AclEntry topic, AclEntry group) {
        return aclConsole.deleteConsumerAcl(topic, group) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData deleteUserAcl(AclEntry entry) {
        return aclConsole.deleteUserAcl(entry) ? ResponseData.create().success() : ResponseData.create().failed();
    }
}
