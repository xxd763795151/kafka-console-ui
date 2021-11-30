package kafka.console

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.admin.ReassignPartitionsCommand
import org.apache.kafka.clients.admin.{ElectLeadersOptions, ListPartitionReassignmentsOptions, PartitionReassignment}
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.{ElectionType, TopicPartition}

import java.util.concurrent.TimeUnit
import java.util.{Collections, Properties}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, ListHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava, SetHasAsJava, SetHasAsScala}

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
            case ex: Throwable => {
                log.error("syncConsumerOffset error.", ex)
                (false, ex.getMessage)
            }
        } finally {
            thatAdmin.close()
        }
    }

    import java.util

    def syncConsumerOffset(groupId: String, topic: String, props: Properties,
        thisMaxOffset: util.Map[TopicPartition, Long],
        thatMinOffset: util.Map[TopicPartition, Long]): (Boolean, String) = {
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
            val thatMinTopicOffset = thatMinOffset.asScala

            val thatDiffOffset = thatMinTopicOffset.map(m => {
                val t = m._1
                thatCommittedOffset.get(t) match {
                    case None => throw new IllegalArgumentException("An unknown error occurred calculating the offset difference")
                    case Some(committedOffset) => (t, committedOffset - m._2)
                }
            })
            // get from this kafka: min offset of topic
            val thisMaxTopicOffset = thisMaxOffset.asScala

            for ((t: TopicPartition, o: Long) <- thisMaxTopicOffset) {
                val res = consumerConsole.resetPartitionToTargetOffset(groupId, t, o + thatDiffOffset.get(t).get)
                if (!res._1) {
                    log.error(s"reset $t offset failed")
                    throw new UnsupportedOperationException(s"reset $t offset failed")
                }
            }
            (true, "")
        } catch {
            case ex: Throwable => {
                log.error("syncConsumerOffset error.", ex)
                (false, ex.getMessage)
            }
        } finally {
            thatAdmin.close()
        }
    }

    /**
     * check partition consistency and fetch the min offset for the that kafka topic, max offset for this kafka.
     *
     * @param groupId group id.
     * @param topic   topic.
     * @param props   other kafka cluster config.
     * @return _1: this max offset, _2: that min offset.
     */
    def checkAndFetchOffset(groupId: String, topic: String,
        props: Properties): (util.Map[TopicPartition, Long], util.Map[TopicPartition, Long]) = {
        val thatAdmin = createAdminClient(props)
        val thatConsumer = new KafkaConsumer(props, new ByteArrayDeserializer, new ByteArrayDeserializer)

        try {
            val thisTopicPartitions = consumerConsole.listSubscribeTopics(groupId).get(topic).asScala.sortBy(_.partition())
            val thatTopicPartitionMap = thatAdmin.listConsumerGroupOffsets(
                groupId
            ).partitionsToOffsetAndMetadata.get(timeoutMs, TimeUnit.MILLISECONDS).asScala.filter(_._1.topic().equals(topic))
            val thatTopicPartitions = thatTopicPartitionMap.keySet.toList.sortBy(_.partition())
            if (thatTopicPartitions != thisTopicPartitions) {
                throw new IllegalStateException("topic partition inconsistent.")
            }
            val thatMinTopicOffset = thatConsumer.beginningOffsets(thatTopicPartitions.asJava)
            val thisMaxTopicOffset = topicConsole.getTopicOffset(topic, thisTopicPartitions.asJava)._2

            (thisMaxTopicOffset, thatMinTopicOffset).asInstanceOf[(util.Map[TopicPartition, Long], util.Map[TopicPartition, Long])]
        } finally {
            thatAdmin.close()
            thatConsumer.close()
        }
    }

    def electPreferredLeader(partitions: util.Set[TopicPartition]): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            admin.electLeaders(ElectionType.PREFERRED, partitions, withTimeoutMs(new ElectLeadersOptions)).all().get()
            (true, "")
        }, e => {
            log.error("electLeaders error.", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    def getTopicPartitions(topic: String): util.Set[TopicPartition] = {
        val topicList = topicConsole.getTopicList(Collections.singleton(topic))
        topicList.asScala.flatMap(_.partitions().asScala.map(t => new TopicPartition(topic, t.partition()))).toSet.asJava
    }

    def modifyInterBrokerThrottle(reassigningBrokers: util.Set[Int],
        interBrokerThrottle: Long): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            ReassignPartitionsCommand.modifyInterBrokerThrottle(admin, reassigningBrokers.asScala.toSet, interBrokerThrottle)
            (true, "")
        }, e => {
            log.error("modifyInterBrokerThrottle error.", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    def clearBrokerLevelThrottles(brokers: util.Set[Int]): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            ReassignPartitionsCommand.clearBrokerLevelThrottles(admin, brokers.asScala.toSet)
            (true, "")
        }, e => {
            log.error("clearBrokerLevelThrottles error.", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    /**
     * current reassigning is active.
     */
    def currentReassignments(): util.Map[TopicPartition, PartitionReassignment] = {
        withAdminClientAndCatchError(admin => {
            admin.listPartitionReassignments(withTimeoutMs(new ListPartitionReassignmentsOptions)).reassignments().get()
        }, e => {
            Collections.emptyMap()
            log.error("listPartitionReassignments error.", e)
        }).asInstanceOf[util.Map[TopicPartition, PartitionReassignment]]
    }

    def cancelPartitionReassignments(reassignments: util.Set[TopicPartition]): util.Map[TopicPartition, Throwable] = {
        withAdminClientAndCatchError(admin => {
            val res = ReassignPartitionsCommand.cancelPartitionReassignments(admin, reassignments.asScala.toSet)
            res.asJava
        }, e => {
            log.error("cancelPartitionReassignments error.", e)
            throw e
        }).asInstanceOf[util.Map[TopicPartition, Throwable]]
    }
}