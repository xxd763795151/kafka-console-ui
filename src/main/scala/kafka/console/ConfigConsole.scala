package kafka.console

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.admin.ConfigCommand.{BrokerDefaultEntityName, BrokerLoggerConfigType}
import kafka.server.ConfigType
import org.apache.kafka.clients.admin.{Admin, Config, ConfigEntry, DescribeConfigsOptions}
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.internals.Topic

import scala.jdk.CollectionConverters.{CollectionHasAsScala, SeqHasAsJava}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-19 20:14:00
 * */
class ConfigConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    import java.util.List


    def getTopicConfig(topic: String) : List[ConfigEntry] = {
        getConfig(ConfigType.Topic, topic)
    }

    def getConfig(entityType: String, entityName: String): List[ConfigEntry] = {
        getResourceConfig(entityType, entityName, true, false).asJava
    }

    private def getResourceConfig(entityType: String, entityName: String, includeSynonyms: Boolean,
        describeAll: Boolean) = {
        def validateBrokerId(): Unit = try entityName.toInt catch {
            case _: NumberFormatException =>
                throw new IllegalArgumentException(s"The entity name for $entityType must be a valid integer broker id, found: $entityName")
        }

        val (configResourceType, dynamicConfigSource) = entityType match {
            case ConfigType.Topic =>
                if (!entityName.isEmpty)
                    Topic.validate(entityName)
                (ConfigResource.Type.TOPIC, Some(ConfigEntry.ConfigSource.DYNAMIC_TOPIC_CONFIG))
            case ConfigType.Broker => entityName match {
                case BrokerDefaultEntityName =>
                    (ConfigResource.Type.BROKER, Some(ConfigEntry.ConfigSource.DYNAMIC_DEFAULT_BROKER_CONFIG))
                case _ =>
                    validateBrokerId()
                    (ConfigResource.Type.BROKER, Some(ConfigEntry.ConfigSource.DYNAMIC_BROKER_CONFIG))
            }
            case BrokerLoggerConfigType =>
                if (!entityName.isEmpty)
                    validateBrokerId()
                (ConfigResource.Type.BROKER_LOGGER, None)
            case entityType => throw new IllegalArgumentException(s"Invalid entity type: $entityType")
        }

        val configSourceFilter = if (describeAll)
            None
        else
            dynamicConfigSource

        val configResource = new ConfigResource(configResourceType, entityName)
        val describeOptions = new DescribeConfigsOptions().includeSynonyms(includeSynonyms)
        val configs = withAdminClientAndCatchError(admin => Some(admin.describeConfigs(Collections.singleton(configResource), describeOptions)
            .all.get(30, TimeUnit.SECONDS)),
            e => {
                log.error("describeConfigs error.", e)
                None
            })

        configs match {
            case None => Seq.empty
            case Some(c: util.Map[ConfigResource, Config]) => c.get(configResource).entries.asScala
                .filter(entry => configSourceFilter match {
                    case Some(configSource) => entry.source == configSource
                    case None => true
                }).toSeq
        }

    }
}
