package kafka.console

import java.util.concurrent.TimeUnit
import java.util.{Collections, Properties}

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.ByteArrayDeserializer

import scala.jdk.CollectionConverters.{CollectionHasAsScala, ListHasAsScala, MapHasAsScala, SeqHasAsJava, SetHasAsScala}

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
            val searchGroupIds = Collections.singleton(groupId)
            val groupDescriptionList = consumerConsole.getConsumerGroupList(searchGroupIds)
            if (groupDescriptionList.isEmpty) {
                throw new IllegalArgumentException("consumer group info is null.")
            }
            for (groupDescription <- groupDescriptionList.asScala) {
                if (groupDescription.members().size() > 0) {
                    log.error("syncConsumerOffset: kafka exist consumer client.")
                    throw new UnsupportedOperationException("exist consumer client.")
                }
            }
            val thatGroupDescriptionList = thatAdmin.describeConsumerGroups(searchGroupIds).all().get(timeoutMs, TimeUnit.MILLISECONDS).values()
            if (groupDescriptionList.isEmpty) {
                throw new IllegalArgumentException("that consumer group info is null.")
            }
            for (thatGroupDescription <- thatGroupDescriptionList.asScala) {
                if (thatGroupDescription.members().size() > 0) {
                    log.error("syncConsumerOffset: that kafka exist consumer client.")
                    throw new UnsupportedOperationException("that kafka exist consumer client.")
                }
            }
            val thisTopicPartitions = consumerConsole.listSubscribeTopics(groupId).get(topic).asScala.sortBy(_.partition())

            val thatTopicPartitionMap = thatAdmin.listConsumerGroupOffsets(
                groupId
            ).partitionsToOffsetAndMetadata.get(timeoutMs, TimeUnit.MILLISECONDS).asScala.filter(_._1.topic().equals(topic))
            val thatTopicPartitions = thatTopicPartitionMap.keySet.toList.sortBy(_.partition())
            if (thatTopicPartitions != thisTopicPartitions) {
                throw new IllegalStateException("topic partition inconsistent.")
            }

            // get from that kafka: min offset of topic and consumer offset for partition
            val thatCommittedOffset = thatTopicPartitionMap.map(m => (m._1, m._2.offset()))
            val thatMinTopicOffset = {
                val consumer = new KafkaConsumer(props, new ByteArrayDeserializer, new ByteArrayDeserializer)
                try {

                    consumer.beginningOffsets(thatTopicPartitions.asJava).asScala
                } finally {
                    consumer.close()
                }
            }

            val thatDiffOffset = thatMinTopicOffset.map(m => {
                val t = m._1
                thatCommittedOffset.get(t) match {
                    case None => throw new IllegalArgumentException("An unknown error occurred calculating the offset difference")
                    case Some(committedOffset) => (t, committedOffset - m._2)
                }
            })
            // get from this kafka: min offset of topic
            val thisMinTopicOffset = topicConsole.getTopicOffset(topic, thisTopicPartitions.asJava)._1.asScala

            for ((t: TopicPartition, o: Long) <- thisMinTopicOffset) {
                val res = consumerConsole.resetPartitionToTargetOffset(groupId, t, o + thatDiffOffset.get(t).get)
                if (!res._1) {
                    log.error(s"reset $t offset failed")
                    throw new UnsupportedOperationException(s"reset $t offset failed")
                }
            }
            (true, "")
        } catch {
            case ex => {
                log.error("syncConsumerOffset error.", ex)
                (false, ex.getMessage)
            }
        } finally {
            thatAdmin.close()
        }
    }
}
