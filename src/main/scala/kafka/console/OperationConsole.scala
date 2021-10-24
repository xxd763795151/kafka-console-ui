package kafka.console

import java.util.Properties
import java.util.concurrent.TimeUnit

import com.xuxd.kafka.console.config.KafkaConfig

import scala.jdk.CollectionConverters.{ListHasAsScala, MapHasAsScala}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-24 23:23:30
 * */
class OperationConsole(config: KafkaConfig, topicConsole: TopicConsole,
    consumerConsole: ConsumerConsole) extends KafkaConsole(config: KafkaConfig) with Logging {

    def syncConsumerOffset(groupId: String, topic: String, props: Properties): (Boolean, String) = {
        val thatAdmin = createAdminClient(props)
        try {
            val thisTopicPartitions = consumerConsole.listSubscribeTopics(groupId).get(topic).asScala.sortBy(_.partition())

            val thatTopicPartitions = thatAdmin.listConsumerGroupOffsets(
                groupId
            ).partitionsToOffsetAndMetadata.get(timeoutMs, TimeUnit.MILLISECONDS).asScala.filter(_._1.topic().equals(topic)).keySet.toList.sortBy(_.partition())
            if (thatTopicPartitions != thisTopicPartitions) {
                throw new IllegalStateException("topic partition inconsistent.")
            }

        } catch {
            case ex => throw ex
        } finally {
            thatAdmin.close()
        }

        (true, "")
    }
}
