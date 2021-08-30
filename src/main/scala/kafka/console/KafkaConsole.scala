package kafka.console

import java.util.Properties

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{Admin, AdminClientConfig}
import org.apache.kafka.common.config.SaslConfigs

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:56:48
 * */
class KafkaConsole(config: KafkaConfig) {

    protected def withAdminClient(f: Admin => Any): Any = {

        val admin = createAdminClient()
        try {
            f(admin)
        } finally {
            admin.close()
        }
    }

    private def createAdminClient(): Admin = {
        val props: Properties = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServer)
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, config.getRequestTimeoutMs())
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, config.getSecurityProtocol())
        props.put(SaslConfigs.SASL_MECHANISM, config.getSaslMechanism())
        props.put(SaslConfigs.SASL_JAAS_CONFIG, config.getSaslJaasConfig())

        Admin.create(props)
    }
}
