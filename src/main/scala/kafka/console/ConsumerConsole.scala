package kafka.console

import java.time.Duration
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, Properties, Set}

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo
import org.apache.kafka.clients.admin.{ConsumerGroupDescription, DeleteConsumerGroupsOptions, ListConsumerGroupsOptions, OffsetSpec}
import org.apache.kafka.clients.consumer.{ConsumerConfig, OffsetAndMetadata, OffsetResetStrategy}
import org.apache.kafka.common.{ConsumerGroupState, TopicPartition}

import scala.beans.BeanProperty
import scala.collection.{Map, mutable}
import scala.jdk.CollectionConverters._

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

    def getConsumerDetail(groupIds: util.Set[String]): util.Collection[TopicPartitionConsumeInfo] = {
        // (groupId -> consumerGroup)
        val consumerGroups = describeConsumerGroups(groupIds)

        val groupOffsets = for ((groupId, consumerGroup) <- consumerGroups) yield {
            // consumer group commit offset
            val commitOffsets = getCommittedOffsets(groupId)

            // get topic offset
            def getPartitionOffset(
                tp: TopicPartition): Option[Long] = commitOffsets.get(tp).filter(_ != null).map(_.offset)

            //            val topicOffsets = Map[TopicPartition, Option[Long]]() ++ (for ((t, o) <- commitOffsets) yield t -> o.offset())

            val endOffsets = withAdminClientAndCatchError(admin => {
                val endOffsets = commitOffsets.keySet.map { topicPartition =>
                    topicPartition -> OffsetSpec.latest
                }.toMap
                admin.listOffsets(endOffsets.asJava).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            }, e => {
                log.error("listOffsets error.", e)
                Collections.emptyMap()
            }).asInstanceOf[util.Map[TopicPartition, ListOffsetsResultInfo]].asScala

            val topicPartitionConsumeInfoMap = commitOffsets.keySet.map(topicPartition => {
                val t = new TopicPartitionConsumeInfo
                t.topicPartition = topicPartition
                t.groupId = consumerGroup.groupId()
                t.consumerOffset = getPartitionOffset(t.topicPartition).get
                endOffsets.get(t.topicPartition) match {
                    case None => t.lag = -1
                    case Some(v) => {
                        t.logEndOffset = v.offset()
                        t.lag = t.logEndOffset - t.consumerOffset
                    }
                }
                t.lag = t.logEndOffset - t.consumerOffset
                (topicPartition, t)
            }).toMap

            consumerGroup.members().asScala.filter(!_.assignment().topicPartitions().isEmpty).foreach(m => {
                m.assignment().topicPartitions().asScala.foreach(topicPartition => {
                    topicPartitionConsumeInfoMap.get(topicPartition) match {
                        case None =>
                        case Some(t) => {
                            t.clientId = m.clientId()
                            t.consumerId = m.consumerId()
                            t.host = m.host()
                        }
                    }

                })
            })

            topicPartitionConsumeInfoMap.map(_._2).asInstanceOf[List[TopicPartitionConsumeInfo]]
        }
        val res = new util.ArrayList[TopicPartitionConsumeInfo]()
        groupOffsets.flatMap(_.toList).foreach(res.add(_))

        res
    }

    def consumeMessageDoNothing(groupId: String, topic: String): (Boolean, String) = {
        val props = new Properties()
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

        withConsumerAndCatchError(consumer => {
            consumer.subscribe(Collections.singletonList(topic))
            for (i <- 1 to 2) {
                consumer.poll(Duration.ofSeconds(1))
            }
            consumer.commitSync()
            (true, "")
        }, e => {
            log.error("subscribe error", e)
            (false, e.getMessage)
        }, props).asInstanceOf[(Boolean, String)]
    }

    def resetOffsetToEarliest(groupId: String, topic: String): (Boolean, String) = {
        resetOffsetToEndpoint(groupId, topic, OffsetResetStrategy.EARLIEST)
    }

    def resetOffsetToEndpoint(groupId: String, topic: String, strategy: OffsetResetStrategy): (Boolean, String) = {
        val props = new Properties()
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, strategy.name().toLowerCase);
        withConsumerAndCatchError(consumer => {
            consumer.subscribe(Collections.singleton(topic))
            consumer.poll(0)
            val partitions = consumer.partitionsFor(topic).asScala.map(p => new TopicPartition(topic, p.partition())).toList
            strategy match {
                case OffsetResetStrategy.EARLIEST => consumer.seekToBeginning(partitions.asJava)
                case OffsetResetStrategy.LATEST => consumer.seekToEnd(partitions.asJava)
            }
            partitions.foreach(consumer.position(_))
            consumer.commitSync()
            (true, "")
        }, e => {
            log.error("resetOffsetToEndpoint error", e)
            (false, e.getMessage)
        }, props).asInstanceOf[(Boolean, String)]
    }

    def resetPartitionToTargetOffset(groupId: String, partition: TopicPartition, offset: Long): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            admin.alterConsumerGroupOffsets(groupId, Map(partition -> new OffsetAndMetadata(offset)).asJava).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        }, e => {
            log.error("resetPartitionToTargetOffset error.", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    private def describeConsumerGroups(groupIds: util.Set[String]): mutable.Map[String, ConsumerGroupDescription] = {
        withAdminClientAndCatchError(admin => {
            admin.describeConsumerGroups(groupIds).describedGroups().asScala.map {
                case (groupId, groupDescriptionFuture) => (groupId, groupDescriptionFuture.get())
            }
        }, e => {
            log.error("describeConsumerGroups error.", e)
            mutable.Map.empty
        }).asInstanceOf[mutable.Map[String, ConsumerGroupDescription]]
    }

    private def getCommittedOffsets(groupId: String): Map[TopicPartition, OffsetAndMetadata] = {
        withAdminClientAndCatchError(admin => {
            admin.listConsumerGroupOffsets(
                groupId
            ).partitionsToOffsetAndMetadata.get.asScala
        }, e => {
            log.error("describeConsumerGroups error.", e)
            mutable.Map.empty
        }).asInstanceOf[Map[TopicPartition, OffsetAndMetadata]]
    }

    class TopicPartitionConsumeInfo {

        @BeanProperty
        var topicPartition: TopicPartition = null

        @BeanProperty
        var groupId = ""

        @BeanProperty
        var consumerOffset: Long = 0L

        @BeanProperty
        var logEndOffset: Long = 0L

        @BeanProperty
        var lag = 0L

        @BeanProperty
        var consumerId = ""

        @BeanProperty
        var clientId = ""

        @BeanProperty
        var host = ""
    }

}
