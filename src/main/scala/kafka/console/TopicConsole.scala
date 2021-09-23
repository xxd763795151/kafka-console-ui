package kafka.console

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, List, Set}

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.{DeleteTopicsOptions, ListTopicsOptions, TopicDescription}

import scala.jdk.CollectionConverters.{CollectionHasAsScala, SetHasAsJava}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 19:52:27
 * */
class TopicConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    /**
     * get all topic name set.
     *
     * @return all topic name set.
     */
    def getTopicNameList(internal: Boolean = true): Set[String] = {
        withAdminClientAndCatchError(admin => admin.listTopics(new ListTopicsOptions().listInternal(internal)).names()
            .get(3000, TimeUnit.MILLISECONDS),
            e => {
                log.error("listTopics error.", e)
                Collections.emptySet()
            }).asInstanceOf[Set[String]]
    }

    /**
     * get all internal topic name set.
     *
     * @return internal topic name set.
     */
    def getInternalTopicNameList(): Set[String] = {
        withAdminClientAndCatchError(admin => admin.listTopics(new ListTopicsOptions().listInternal(true)).listings()
            .get(3000, TimeUnit.MILLISECONDS).asScala.filter(_.isInternal).map(_.name()).toSet[String].asJava,
            e => {
                log.error("listInternalTopics error.", e)
                Collections.emptySet()
            }).asInstanceOf[Set[String]]
    }

    def getTopicList(topics: Set[String]): List[TopicDescription] = {
        if (topics == null || topics.isEmpty) {
            Collections.emptyList()
        } else {
            withAdminClientAndCatchError(admin => new util.ArrayList[TopicDescription](admin.describeTopics(topics).all().get().values()), e => {
                log.error("describeTopics error.", e)
                Collections.emptyList()
            }).asInstanceOf[List[TopicDescription]]
        }
    }

    /**
     * delete topic by topic name.
     *
     * @param topic topic name.
     * @return result or : fail message.
     */
    def deleteTopic(topic: String): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            admin.deleteTopics(Collections.singleton(topic), new DeleteTopicsOptions().retryOnQuotaViolation(false)).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        },
            e => {
                log.error("delete topic error, topic: " + topic, e)
                (false, e.getMessage)
            }).asInstanceOf[(Boolean, String)]
    }
}
