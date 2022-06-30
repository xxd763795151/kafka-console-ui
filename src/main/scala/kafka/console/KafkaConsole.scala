package kafka.console

import com.xuxd.kafka.console.config.{ContextConfigHolder, KafkaConfig}
import kafka.zk.{AdminZkClient, KafkaZkClient}
import org.apache.kafka.clients.admin._
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer, OffsetAndMetadata}
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.requests.ListOffsetsResponse
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringSerializer}
import org.apache.kafka.common.utils.Time
import org.slf4j.{Logger, LoggerFactory}

import java.util.Properties
import scala.collection.{Map, Seq}
import scala.jdk.CollectionConverters.{MapHasAsJava, MapHasAsScala}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:56:48
 * */
class KafkaConsole(config: KafkaConfig) {

//    protected val timeoutMs: Int = config.getRequestTimeoutMs

    protected def withAdminClient(f: Admin => Any): Any = {

        val admin = createAdminClient()
        try {
            f(admin)
        } finally {
            admin.close()
        }
    }

    protected def withAdminClientAndCatchError(f: Admin => Any, eh: Exception => Any): Any = {
        try {
            withAdminClient(f)
        } catch {
            case er: Exception => eh(er)
        }
    }

    protected def withConsumerAndCatchError(f: KafkaConsumer[Array[Byte], Array[Byte]] => Any, eh: Exception => Any,
        extra: Properties = new Properties()): Any = {
        val props = getProps()
        props.putAll(extra)
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, String.valueOf(System.currentTimeMillis()))
        val consumer = new KafkaConsumer(props, new ByteArrayDeserializer, new ByteArrayDeserializer)
        try {
            f(consumer)
        } catch {
            case er: Exception => eh(er)
        }
        finally {
            consumer.close()
        }
    }

    protected def withProducerAndCatchError(f: KafkaProducer[String, String] => Any, eh: Exception => Any,
        extra: Properties = new Properties()): Any = {
        val props = getProps()
        props.putAll(extra)
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, String.valueOf(System.currentTimeMillis()))
        val producer = new KafkaProducer[String, String](props, new StringSerializer, new StringSerializer)
        try {
            f(producer)
        } catch {
            case er: Exception => eh(er)
        }
        finally {
            producer.close()
        }
    }

    protected def withByteProducerAndCatchError(f: KafkaProducer[Array[Byte], Array[Byte]] => Any, eh: Exception => Any,
        extra: Properties = new Properties()): Any = {
        val props = getProps()
        props.putAll(extra)
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, String.valueOf(System.currentTimeMillis()))
        val producer = new KafkaProducer[Array[Byte], Array[Byte]](props, new ByteArraySerializer, new ByteArraySerializer)
        try {
            f(producer)
        } catch {
            case er: Exception => eh(er)
        }
        finally {
            producer.close()
        }
    }

    @Deprecated
    protected def withZKClient(f: AdminZkClient => Any): Any = {
//        val zkClient = KafkaZkClient(config.getZookeeperAddr, false, 30000, 30000, Int.MaxValue, Time.SYSTEM)
        // 3.x
//        val zkClient = KafkaZkClient(config.getZookeeperAddr, false, 30000, 30000, Int.MaxValue, Time.SYSTEM, new ZKClientConfig(), "KafkaZkClient")
//        val adminZkClient = new AdminZkClient(zkClient)
//        try {
//            f(adminZkClient)
//        } finally {
//            zkClient.close()
//        }
    }

    protected def createAdminClient(props: Properties): Admin = {
        Admin.create(props)
    }

    protected def withTimeoutMs[T <: AbstractOptions[T]](options: T) = {
        options.timeoutMs(ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs())
    }

    private def createAdminClient(): Admin = {
        Admin.create(getProps())
    }

    private def getProps(): Properties = {
        val props: Properties = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, ContextConfigHolder.CONTEXT_CONFIG.get().getBootstrapServer())
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs())
        props.putAll(ContextConfigHolder.CONTEXT_CONFIG.get().getProperties())
        props
    }
}

object KafkaConsole {
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    def getCommittedOffsets(admin: Admin, groupId: String,
        timeoutMs: Integer): Map[TopicPartition, OffsetAndMetadata] = {
        admin.listConsumerGroupOffsets(
            groupId, new ListConsumerGroupOffsetsOptions().timeoutMs(timeoutMs)
        ).partitionsToOffsetAndMetadata.get.asScala
    }

    def getLogTimestampOffsets(admin: Admin, topicPartitions: Seq[TopicPartition],
        timestamp: java.lang.Long, timeoutMs: Integer): Map[TopicPartition, OffsetAndMetadata] = {
        val timestampOffsets = topicPartitions.map { topicPartition =>
            topicPartition -> OffsetSpec.forTimestamp(timestamp)
        }.toMap
        val offsets = admin.listOffsets(
            timestampOffsets.asJava,
            new ListOffsetsOptions().timeoutMs(timeoutMs)
        ).all.get
        val (successfulOffsetsForTimes, unsuccessfulOffsetsForTimes) =
            offsets.asScala.partition(_._2.offset != ListOffsetsResponse.UNKNOWN_OFFSET)

        val successfulLogTimestampOffsets = successfulOffsetsForTimes.map {
            case (topicPartition, listOffsetsResultInfo) => topicPartition -> new OffsetAndMetadata(listOffsetsResultInfo.offset)
        }.toMap

        unsuccessfulOffsetsForTimes.foreach { entry =>
            log.warn(s"\nWarn: Partition " + entry._1.partition() + " from topic " + entry._1.topic() +
                " is empty. Falling back to latest known offset.")
        }

        successfulLogTimestampOffsets ++ getLogEndOffsets(admin, unsuccessfulOffsetsForTimes.keySet.toSeq, timeoutMs)
    }

    def getLogEndOffsets(admin: Admin,
        topicPartitions: Seq[TopicPartition], timeoutMs: Integer): Predef.Map[TopicPartition, OffsetAndMetadata] = {
        val endOffsets = topicPartitions.map { topicPartition =>
            topicPartition -> OffsetSpec.latest
        }.toMap
        val offsets = admin.listOffsets(
            endOffsets.asJava,
            new ListOffsetsOptions().timeoutMs(timeoutMs)
        ).all.get
        val res = topicPartitions.map { topicPartition =>
            Option(offsets.get(topicPartition)) match {
                case Some(listOffsetsResultInfo) => topicPartition -> new OffsetAndMetadata(listOffsetsResultInfo.offset)
                case _ =>
                    throw new IllegalArgumentException
            }
        }.toMap
        res
    }
}
