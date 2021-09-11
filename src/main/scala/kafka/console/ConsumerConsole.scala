package kafka.console

import java.util
import java.util.{Collections, Set}

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.{ConsumerGroupDescription, DeleteConsumerGroupsOptions, ListConsumerGroupsOptions}
import org.apache.kafka.common.ConsumerGroupState

import scala.jdk.CollectionConverters.{CollectionHasAsScala, SetHasAsJava}

/**
 * kafka-console-ui. kafka consumer console.
 *
 * @author xuxd
 * @date 2021-09-10 17:19:31
 * */
class ConsumerConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    def getConsumerGroupIdList(states: Set[ConsumerGroupState]): Set[String] = {

        withAdminClientAndCatchError(admin => admin.listConsumerGroups(new ListConsumerGroupsOptions().inStates(states)).all().get()
            .asScala.map(_.groupId()).toSet.asJava,
            e => {
                log.error("listConsumerGroups error.", e)
                Collections.emptySet()
            }).asInstanceOf[Set[String]]
    }

    def getConsumerGroupList(groupIds: util.Collection[String]): Set[ConsumerGroupDescription] = {
        val searchGroupIds: Set[String] = if (groupIds == null || groupIds.isEmpty) getConsumerGroupIdList(null) else new util.HashSet[String](groupIds)
        withAdminClientAndCatchError(admin => new util.HashSet[ConsumerGroupDescription](admin.describeConsumerGroups(searchGroupIds).all().get().values()),
            e => {
                log.error("listConsumerGroups error.", e)
                Collections.emptySet()
            }).asInstanceOf[Set[ConsumerGroupDescription]]
    }

    def deleteConsumerGroups(groupIds: util.Collection[String]): (Boolean, String) = {
        if (groupIds == null || groupIds.isEmpty) {
            (false, "group id is empty.")
        } else {
            withAdminClientAndCatchError(admin => {
                admin.deleteConsumerGroups(groupIds, new DeleteConsumerGroupsOptions).all().get()
                (true, "")
            }
                , e => {
                    log.error("deleteConsumerGroups error.", e)
                    (false, e.getMessage)
                }).asInstanceOf[(Boolean, String)]
        }
    }
}
