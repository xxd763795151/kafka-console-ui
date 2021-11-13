package kafka.console

import java.util.Properties

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.zk.{AdminZkClient, KafkaZkClient}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{AbstractOptions, Admin, AdminClientConfig}
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.utils.Time

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:56:48
 * */
class KafkaConsole(config: KafkaConfig) {

    protected val timeoutMs: Int = config.getRequestTimeoutMs

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

    protected def withZKClient(f: AdminZkClient => Any): Any = {
        val zkClient = KafkaZkClient(config.getZookeeperAddr, false, 30000, 30000, Int.MaxValue, Time.SYSTEM)
        val adminZkClient = new AdminZkClient(zkClient)
        try {
            f(adminZkClient)
        } finally {
            zkClient.close()
        }
    }

    protected def createAdminClient(props: Properties): Admin = {
        Admin.create(props)
    }

    protected def withTimeoutMs[T <: AbstractOptions[T]](options: T) = {
        options.timeoutMs(timeoutMs)
    }

    private def createAdminClient(): Admin = {
        Admin.create(getProps())
    }

    private def getProps(): Properties = {
        val props: Properties = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServer)
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getRequestTimeoutMs())
        if (config.isEnableAcl) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.getSecurityProtocol())
            props.put(SaslConfigs.SASL_MECHANISM, config.getSaslMechanism())
            props.put(SaslConfigs.SASL_JAAS_CONFIG, config.getSaslJaasConfig())
        }
        props
    }
}
