package kafka.console

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.admin.ReassignPartitionsCommand.compareTopicPartitions
import kafka.utils.Json
import org.apache.kafka.clients.admin._
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.apache.kafka.common.{TopicPartition, TopicPartitionReplica}

import java.util
import java.util.concurrent.{ExecutionException, TimeUnit}
import java.util.{Collections, List, Set}
import scala.collection.{Map, Seq}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava, SetHasAsJava}

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
            .get(timeoutMs, TimeUnit.MILLISECONDS),
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
            .get(timeoutMs, TimeUnit.MILLISECONDS).asScala.filter(_.isInternal).map(_.name()).toSet[String].asJava,
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

    /**
     * get topic begin offset and end offset.
     *
     * @param topic      topic name.
     * @param partitions topic partition info list.
     * @return partition ->  begin offset and end offset.
     */
    def getTopicOffset(topic: String,
        partitions: List[TopicPartition]): (util.Map[TopicPartition, Long], util.Map[TopicPartition, Long]) = {

        withConsumerAndCatchError(consumer => {
            val beginOffsets = consumer.beginningOffsets(partitions)
            val endOffsets = consumer.endOffsets(partitions)
            (beginOffsets, endOffsets)
        }, e => {
            log.error("getTopicOffset error, topic: " + topic, e)
            (Collections.emptyMap(), Collections.emptyMap())
        }).asInstanceOf[(util.Map[TopicPartition, Long], util.Map[TopicPartition, Long])]
    }

    /**
     * create topic.
     */
    def createTopic(topic: NewTopic): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            val createResult = admin.createTopics(Collections.singleton(topic), new CreateTopicsOptions().retryOnQuotaViolation(false))
            createResult.all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        }, e => {
            log.error("create topic error, topic: " + topic.name(), e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    /**
     * create new partition.
     */
    def createPartitions(newPartitions: util.Map[String, NewPartitions]): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            admin.createPartitions(newPartitions,
                new CreatePartitionsOptions().retryOnQuotaViolation(false)).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        }, e => {
            log.error("create partition error, ", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    def getCurrentReplicaAssignmentJson(topic: String): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            val json = formatAsReassignmentJson(getReplicaAssignmentForTopics(admin, Seq(topic)), Map.empty)
            (true, json)
        }, e => {
            log.error("getCurrentReplicaAssignmentJson error, ", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    /**
     * Get the current replica assignments for some topics.
     *
     * @param adminClient The AdminClient to use.
     * @param topics      The topics to get information about.
     * @return A map from partitions to broker assignments.
     *         If any topic can't be found, an exception will be thrown.
     */
    private def getReplicaAssignmentForTopics(adminClient: Admin,
        topics: Seq[String])
    : Map[TopicPartition, Seq[Int]] = {
        describeTopics(adminClient, topics.toSet.asJava).flatMap {
            case (topicName, topicDescription) => topicDescription.partitions.asScala.map { info =>
                (new TopicPartition(topicName, info.partition), info.replicas.asScala.map(_.id).toSeq)
            }
        }
    }

    private def formatAsReassignmentJson(partitionsToBeReassigned: Map[TopicPartition, Seq[Int]],
        replicaLogDirAssignment: Map[TopicPartitionReplica, String]): String = {
        Json.encodeAsString(Map(
            "version" -> 1,
            "partitions" -> partitionsToBeReassigned.keySet.toBuffer.sortWith(compareTopicPartitions).map {
                tp =>
                    val replicas = partitionsToBeReassigned(tp)
                    Map(
                        "topic" -> tp.topic,
                        "partition" -> tp.partition,
                        "replicas" -> replicas.asJava
                    ).asJava
            }.asJava
        ).asJava)
    }

    private def describeTopics(adminClient: Admin,
        topics: Set[String])
    : Map[String, TopicDescription] = {
        adminClient.describeTopics(topics).values.asScala.map { case (topicName, topicDescriptionFuture) =>
            try topicName -> topicDescriptionFuture.get
            catch {
                case t: ExecutionException if t.getCause.isInstanceOf[UnknownTopicOrPartitionException] =>
                    throw new ExecutionException(
                        new UnknownTopicOrPartitionException(s"Topic $topicName not found."))
            }
        }
    }
}
