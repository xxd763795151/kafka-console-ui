package com.xuxd.kafka.console.service;

import com.xuxd.kafka.console.beans.AclEntry;
import com.xuxd.kafka.console.beans.ResponseData;
import java.util.Set;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:44:06
 **/
public interface AclService {

    ResponseData<Set<String>> getUserList();

    ResponseData addOrUpdateUser(String name, String pass);

    ResponseData deleteUser(String name);

    ResponseData deleteUserAndAuth(String name);

    ResponseData getAclDetailList(AclEntry entry);

    ResponseData getAclList(AclEntry entry);

    ResponseData deleteAcl(AclEntry entry);

    ResponseData addAcl(AclEntry entry);

    ResponseData addProducerAcl(AclEntry entry);

    ResponseData addConsumerAcl(AclEntry topic, AclEntry group);

    ResponseData deleteProducerAcl(AclEntry entry);

    ResponseData deleteConsumerAcl(AclEntry topic, AclEntry group);

    ResponseData deleteUserAcl(AclEntry entry);

    ResponseData getOperationList();

    ResponseData getUserDetail(String username);

}
