package kafka.console

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition

import java.time.Duration
import java.util
import java.util.Properties
import scala.collection.immutable
import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsScala}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-12-11 09:39:40
 * */
class MessageConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    def searchBy(partitions: util.Collection[TopicPartition], startTime: Long, endTime: Long,
        maxNums: Int): util.List[ConsumerRecord[Array[Byte], Array[Byte]]] = {
        var startOffTable: immutable.Map[TopicPartition, Long] = Map.empty
        var endOffTable: immutable.Map[TopicPartition, Long] = Map.empty
        withAdminClientAndCatchError(admin => {
            val startTable = KafkaConsole.getLogTimestampOffsets(admin, partitions.asScala.toSeq, startTime, timeoutMs)
            startOffTable = startTable.map(t2 => (t2._1, t2._2.offset())).toMap

            endOffTable = KafkaConsole.getLogTimestampOffsets(admin, partitions.asScala.toSeq, endTime, timeoutMs)
                .map(t2 => (t2._1, t2._2.offset())).toMap
        }, e => {
            log.error("getLogTimestampOffsets error.", e)
            throw new RuntimeException("getLogTimestampOffsets error", e)
        })
        var terminate: Boolean = (startOffTable == endOffTable)
        val res = new util.LinkedList[ConsumerRecord[Array[Byte], Array[Byte]]]()
        // 如果最小和最大偏移一致，就结束
        if (!terminate) {

            val arrive = new util.HashSet[TopicPartition](partitions)
            val props = new Properties()
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            withConsumerAndCatchError(consumer => {
                consumer.assign(partitions)
                for ((tp, off) <- startOffTable) {
                    consumer.seek(tp, off)
                }

                // 终止条件
                // 1.所有查询分区达都到最大偏移的时候
                while (!terminate) {
                    // 达到查询的最大条数
                    if (res.size() >= maxNums) {
                        terminate = true
                    } else {
                        val records = consumer.poll(Duration.ofMillis(timeoutMs))

                        for ((tp, endOff) <- endOffTable) {
                            if (!terminate) {
                                val recordList = records.records(tp)
                                if (recordList.isEmpty) {
                                    arrive.remove(tp)
                                } else {
                                    val first = recordList.get(0)
                                    if (first.offset() >= endOff) {
                                        arrive.remove(tp)
                                    } else {
                                        res.addAll(recordList)
                                        if (recordList.get(recordList.size() - 1).offset() >= endOff) {
                                            arrive.remove(tp)
                                        }
                                    }
                                }

                                if (arrive.isEmpty) {
                                    terminate = true;
                                }
                            }
                        }
                    }
                }
            }, e => {
                log.error("searchBy time error.", e)
            })
        }

        res
    }

    def searchBy(
        tp2o: util.Map[TopicPartition, Long]): util.Map[TopicPartition, ConsumerRecord[Array[Byte], Array[Byte]]] = {
        val props = new Properties()
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
        val res = new util.HashMap[TopicPartition, ConsumerRecord[Array[Byte], Array[Byte]]]()
        withConsumerAndCatchError(consumer => {
            var tpSet = tp2o.keySet()
            val tpSetCopy = new util.HashSet[TopicPartition](tpSet)
            val endOffsets = consumer.endOffsets(tpSet)
            val beginOffsets = consumer.beginningOffsets(tpSet)
            for ((tp, off) <- tp2o.asScala) {
                val endOff = endOffsets.get(tp)
                //                if (endOff <= off) {
                //                    consumer.seek(tp, endOff)
                //                    tpSetCopy.remove(tp)
                //                } else {
                //                    consumer.seek(tp, off)
                //                }
                val beginOff = beginOffsets.get(tp)
                if (off < beginOff || off >= endOff) {
                    tpSetCopy.remove(tp)
                }
            }

            tpSet = tpSetCopy
            consumer.assign(tpSet)
            tpSet.asScala.foreach(tp => {
                consumer.seek(tp, tp2o.get(tp))
            })

            var terminate = tpSet.isEmpty
            while (!terminate) {
                val records = consumer.poll(Duration.ofMillis(timeoutMs))
                val tps = new util.HashSet(tpSet).asScala
                for (tp <- tps) {
                    if (!res.containsKey(tp)) {
                        val recordList = records.records(tp)
                        if (!recordList.isEmpty) {
                            val record = recordList.get(0)
                            res.put(tp, record)
                            tpSet.remove(tp)
                        }
                    }
                    if (tpSet.isEmpty) {
                        terminate = true
                    }
                }
            }

        }, e => {
            log.error("searchBy offset error.", e)
        })
        res
    }
}