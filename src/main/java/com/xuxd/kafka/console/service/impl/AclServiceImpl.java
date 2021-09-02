package com.xuxd.kafka.console.service.impl;

import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.CounterList;
import com.xuxd.kafka.console.beans.CounterMap;
import com.xuxd.kafka.console.beans.ResponseData;
import com.xuxd.kafka.console.config.KafkaConfig;
import com.xuxd.kafka.console.service.AclService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import kafka.console.KafkaAclConsole;
import kafka.console.KafkaConfigConsole;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.admin.UserScramCredentialsDescription;
import org.apache.kafka.common.acl.AclBinding;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.Tuple2;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:44:40
 **/
@Slf4j
@Service
public class AclServiceImpl implements AclService, SmartInitializingSingleton {

    @Autowired
    private KafkaConfigConsole configConsole;

    @Autowired
    private KafkaAclConsole aclConsole;

    @Autowired
    private KafkaConfig kafkaConfig;

    @Override public ResponseData<Set<String>> getUserList() {
        try {
            return ResponseData.create(Set.class).data(configConsole.getUserList(null)).success();
        } catch (Exception e) {
            log.error("getUserList error.", e);
            return ResponseData.create().failed();
        }
    }

    @Override public ResponseData addOrUpdateUser(String name, String pass) {
        log.info("add or update user, username: {}, password: {}", name, pass);
        return configConsole.addOrUpdateUser(name, pass) ? ResponseData.create().success() : ResponseData.create().failed();
    }

    @Override public ResponseData deleteUser(String name) {
        log.info("delete user: {}", name);
        Tuple2<Object, String> tuple2 = configConsole.deleteUser(name);
        return (boolean)tuple2._1() ? ResponseData.create().success() : ResponseData.create().failed(tuple2._2());
    }

    @Override public ResponseData deleteUserAndAuth(String name) {
        log.info("delete user and authority: {}", name);
        AclEntry entry = new AclEntry();
        entry.setPrincipal(name);
        if ( aclConsole.deleteUserAcl(entry)) {
            Tuple2<Object, String> delUR = configConsole.deleteUser(name);
            if (!((boolean)delUR._1())) {
                return ResponseData.create().failed("用户权限删除成功，但是用户信息删除失败: " + delUR._2());
            }
        } else {
            return ResponseData.create().failed("删除用户权限失败");
        }

        return ResponseData.create().success();
    }

    @Override public ResponseData getAclList() {
        List<AclBinding> aclBindingList = aclConsole.getAclList(null);

        return ResponseData.create().data(new CounterList<>(aclBindingList.stream().map(x -> AclEntry.valueOf(x)).collect(Collectors.toList()))).success();
    }

    @Override public ResponseData getAclList(AclEntry entry) {
        List<AclBinding> aclBindingList = entry.isNull() ? aclConsole.getAclList(null) : aclConsole.getAclList(entry);
        List<AclEntry> entryList = aclBindingList.stream().map(x -> AclEntry.valueOf(x)).collect(Collectors.toList());
        Map<String, List<AclEntry>> entryMap = entryList.stream().collect(Collectors.groupingBy(AclEntry::getPrincipal));
        Map<String, Map<String, List<AclEntry>>> resultMap = new HashMap<>();
        entryMap.forEach((k, v) -> {
            Map<String, List<AclEntry>> map = v.stream().collect(Collectors.groupingBy(e -> e.getResourceType() + "#" + e.getName()));
            resultMap.put(k, map);
        });
        if (entry.isNull() || StringUtils.isNotBlank(entry.getPrincipal())) {
            Map<String, UserScramCredentialsDescription> detailList = configConsole.getUserDetailList(StringUtils.isNotBlank(entry.getPrincipal()) ? Collections.singletonList(entry.getPrincipal()) : null);

            detailList.values().forEach(u -> {
                if (!resultMap.containsKey(u.name()) && !u.credentialInfos().isEmpty()) {
                    resultMap.put(u.name(), Collections.emptyMap());
                }
            });
        }

        return ResponseData.create().data(new CounterMap<>(resultMap)).success();
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

    @Override public void afterSingletonsInstantiated() {
        if (kafkaConfig.isAdminCreate()) {
            log.info("Start create admin user, username: {}, password: {}", kafkaConfig.getAdminUsername(), kafkaConfig.getAdminPassword());
            boolean done = configConsole.addOrUpdateUserWithZK(kafkaConfig.getAdminUsername(), kafkaConfig.getAdminPassword());
            if (!done) {
                log.error("Create admin failed.");
                throw new IllegalStateException();
            }
        }
    }
}
