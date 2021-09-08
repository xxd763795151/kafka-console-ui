package kafka.console

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, List, Set}

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.{ListTopicsOptions, TopicDescription}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-09-08 19:52:27
 * */
class TopicConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    def getTopicNameList(): Set[String] = {
        withAdminClientAndCatchError(admin => admin.listTopics(new ListTopicsOptions().listInternal(true)).names()
            .get(3000, TimeUnit.MILLISECONDS),
            e => {
                log.error("listTopics error.", e)
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
}
