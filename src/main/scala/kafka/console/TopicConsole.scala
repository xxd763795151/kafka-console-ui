package kafka.console

import com.xuxd.kafka.console.config.{ContextConfigHolder, KafkaConfig}
import kafka.admin.ReassignPartitionsCommand._
import kafka.utils.Json
import org.apache.kafka.clients.admin._
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.apache.kafka.common.utils.Time
import org.apache.kafka.common.{TopicPartition, TopicPartitionReplica}

import java.util
import java.util.concurrent.{ExecutionException, TimeUnit}
import java.util.{Collections, List, Set}
import scala.collection.{Map, Seq}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsJava, MapHasAsScala, SeqHasAsJava, SetHasAsJava}

/**
 * kafka topic console.
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
        val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
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
        val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
        withAdminClientAndCatchError(admin => admin.listTopics(new ListTopicsOptions().listInternal(true)).listings()
            .get(timeoutMs, TimeUnit.MILLISECONDS).asScala.filter(_.isInternal).map(_.name()).toSet[String].asJava,
            e => {
                log.error("listInternalTopics error.", e)
                Collections.emptySet()
            }).asInstanceOf[Set[String]]
    }

    /**
     * get topic list by topic name list.
     *
     * @param topics topic name list.
     * @return topic list.
     */
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
     * @param topics topic name list.
     * @return result or : fail message.
     */
    def deleteTopics(topics: util.Collection[String]): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
            admin.deleteTopics(topics, new DeleteTopicsOptions().retryOnQuotaViolation(false)).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        },
            e => {
                log.error("delete topic error, topic: " + topics, e)
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
            val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
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
            val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
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

    def updateReplicas(reassignmentJson: String, interBrokerThrottle: Long = -1L): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            executeAssignment(admin, reassignmentJson, interBrokerThrottle)
            (true, "")
        }, e => {
            log.error("executeAssignment error, ", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    /**
     * Copy and modify from @{link kafka.admin.ReassignPartitionsCommand#executeAssignment}.
     */
    def executeAssignment(adminClient: Admin,
        reassignmentJson: String,
        interBrokerThrottle: Long = -1L,
        logDirThrottle: Long = -1L,
        timeoutMs: Long = 30000L,
        time: Time = Time.SYSTEM): Unit = {
        val (proposedParts, proposedReplicas) = parseExecuteAssignmentArgs(reassignmentJson)
        val currentReassignments = adminClient.
            listPartitionReassignments().reassignments().get().asScala
        // If there is an existing assignment
        // This helps avoid surprising users.
        if (currentReassignments.nonEmpty) {
            throw new TerseReassignmentFailureException("Cannot execute because there is an existing partition assignment.")
        }
        verifyBrokerIds(adminClient, proposedParts.values.flatten.toSet)
        val currentParts = getReplicaAssignmentForPartitions(adminClient, proposedParts.keySet.toSet)
        log.info("currentPartitionReplicaAssignment: " + currentPartitionReplicaAssignmentToString(proposedParts, currentParts))
        log.info(s"newPartitionReplicaAssignment: $reassignmentJson")

        if (interBrokerThrottle >= 0 || logDirThrottle >= 0) {

            if (interBrokerThrottle >= 0) {
                val moveMap = calculateProposedMoveMap(currentReassignments, proposedParts, currentParts)
                modifyReassignmentThrottle(adminClient, moveMap)
            }

            if (logDirThrottle >= 0) {
                val movingBrokers = calculateMovingBrokers(proposedReplicas.keySet.toSet)
                modifyLogDirThrottle(adminClient, movingBrokers, logDirThrottle)
            }
        }

        // Execute the partition reassignments.
        val errors = alterPartitionReassignments(adminClient, proposedParts)
        if (errors.nonEmpty) {
            throw new TerseReassignmentFailureException(
                "Error reassigning partition(s):%n%s".format(
                    errors.keySet.toBuffer.sortWith(compareTopicPartitions).map { part =>
                        s"$part: ${errors(part).getMessage}"
                    }.mkString(System.lineSeparator())))
        }
        if (proposedReplicas.nonEmpty) {
            executeMoves(adminClient, proposedReplicas, timeoutMs, time)
        }
    }

    def configThrottle(topic: String, partitions: util.List[Integer]): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            val throttles = {
                if (partitions.get(0) == -1) {
                    Map(topic -> "*")
                } else {
                    val topicDescription = admin.describeTopics(Collections.singleton(topic), withTimeoutMs(new DescribeTopicsOptions))
                        .all().get().values().asScala.toList

                    def convert(partition: Integer, replicas: scala.List[Int]): String = {
                        replicas.map("%d:%d".format(partition, _)).toSet.mkString(",")
                    }

                    val ptor = topicDescription.head.partitions().asScala.map(info => (info.partition(), info.replicas().asScala.map(_.id()))).toMap
                    val conf = partitions.asScala.map(partition => convert(partition, ptor.get(partition) match {
                        case Some(v) => v.toList
                        case None => throw new IllegalArgumentException
                    })).toList
                    Map(topic -> conf.mkString(","))
                }
            }
            modifyTopicThrottles(admin, throttles, throttles)
            (true, "")
        }, e => {
            log.error("configThrottle error, ", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    def clearThrottle(topic: String): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            clearTopicLevelThrottles(admin, Collections.singleton(topic).asScala.toSet)
            (true, "")
        }, e => {
            log.error("clearThrottle error, ", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    def getOffsetForTimestamp(topic: String, timestamp: java.lang.Long): util.Map[TopicPartition, java.lang.Long] = {
        withAdminClientAndCatchError(admin => {
            val partitions = describeTopics(admin, Collections.singleton(topic)).get(topic) match {
                case Some(topicDescription: TopicDescription) => topicDescription.partitions()
                    .asScala.map(info => new TopicPartition(topic, info.partition())).toSeq
                case None => throw new IllegalArgumentException("topic is not exist.")
            }
            val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
            val offsetMap = KafkaConsole.getLogTimestampOffsets(admin, partitions, timestamp, timeoutMs)
            offsetMap.map(tuple2 => (tuple2._1, tuple2._2.offset())).toMap.asJava
        }, e => {
            log.error("clearThrottle error, ", e)
            Collections.emptyMap()
        }).asInstanceOf[util.Map[TopicPartition, java.lang.Long]]
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

    private def modifyReassignmentThrottle(admin: Admin, moveMap: MoveMap): Unit = {
        val leaderThrottles = calculateLeaderThrottles(moveMap)
        val followerThrottles = calculateFollowerThrottles(moveMap)
        modifyTopicThrottles(admin, leaderThrottles, followerThrottles)
    }
}
