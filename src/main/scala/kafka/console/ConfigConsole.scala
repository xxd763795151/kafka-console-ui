package kafka.console

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.admin.ConfigCommand.BrokerLoggerConfigType
import kafka.server.ConfigType
import org.apache.kafka.clients.admin.{AlterConfigOp, Config, ConfigEntry, DescribeConfigsOptions}
import org.apache.kafka.common.config.ConfigResource
import org.apache.kafka.common.internals.Topic

import scala.jdk.CollectionConverters.{CollectionHasAsScala, MapHasAsJava, SeqHasAsJava}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-19 20:14:00
 * */
class ConfigConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    import java.util.List

    def getTopicConfig(topic: String): List[ConfigEntry] = {
        getConfig(ConfigType.Topic, topic)
    }

    def getBrokerConfig(broker: String): List[ConfigEntry] = {
        getConfig(ConfigType.Broker, broker)
    }

    def setBrokerConfig(broker: String, entry: ConfigEntry): (Boolean, String) = {
        alterConfig(ConfigType.Broker, broker, entry, AlterConfigOp.OpType.SET)
    }

    def deleteBrokerConfig(broker: String, entry: ConfigEntry): (Boolean, String) = {
        alterConfig(ConfigType.Broker, broker, entry, AlterConfigOp.OpType.DELETE)
    }

    def setTopicConfig(topic: String, entry: ConfigEntry): (Boolean, String) = {
        alterConfig(ConfigType.Topic, topic, entry, AlterConfigOp.OpType.SET)
    }

    def deleteTopicConfig(topic: String, entry: ConfigEntry): (Boolean, String) = {
        alterConfig(ConfigType.Topic, topic, entry, AlterConfigOp.OpType.DELETE)
    }

    def getConfig(entityType: String, entityName: String): List[ConfigEntry] = {
        getResourceConfig(entityType, entityName, false).asJava
    }

    def alterConfig(entityType: String, entityName: String, entry: ConfigEntry,
        opType: AlterConfigOp.OpType): (Boolean, String) = {
        withAdminClientAndCatchError(admin => {
            val configResource = new ConfigResource(getResourceTypeAndValidate(entityType, entityName), entityName)

            val config = Map(configResource -> Collections.singletonList(new AlterConfigOp(entry, opType)).asInstanceOf[util.Collection[AlterConfigOp]])
            admin.incrementalAlterConfigs(config.asJava).all().get(timeoutMs, TimeUnit.MILLISECONDS)
            (true, "")
        }, e => {
            log.error("alter config error.", e)
            (false, e.getMessage)
        }).asInstanceOf[(Boolean, String)]
    }

    private def getResourceConfig(entityType: String, entityName: String, includeSynonyms: Boolean) = {
        val configResourceType = getResourceTypeAndValidate(entityType, entityName)

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
            case Some(c: util.Map[ConfigResource, Config]) => c.get(configResource).entries.asScala.toSeq
        }

    }

    private def getResourceTypeAndValidate(entityType: String, entityName: String): ConfigResource.Type = {
        def validateBrokerId(): Unit = try entityName.toInt catch {
            case _: NumberFormatException =>
                throw new IllegalArgumentException(s"The entity name for $entityType must be a valid integer broker id, found: $entityName")
        }

        val configResourceType = entityType match {
            case ConfigType.Topic =>
                if (!entityName.isEmpty)
                    Topic.validate(entityName)
                ConfigResource.Type.TOPIC
            case ConfigType.Broker =>
                validateBrokerId()
                ConfigResource.Type.BROKER
            case BrokerLoggerConfigType =>
                if (!entityName.isEmpty)
                    validateBrokerId()
                ConfigResource.Type.BROKER_LOGGER
            case entityType => throw new IllegalArgumentException(s"Invalid entity type: $entityType")
        }
        configResourceType
    }
}
