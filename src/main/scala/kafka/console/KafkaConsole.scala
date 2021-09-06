package kafka.console

import java.util.Properties

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.zk.{AdminZkClient, KafkaZkClient}
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.admin.{Admin, AdminClientConfig}
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.utils.Time

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

    protected def withZKClient(f: AdminZkClient => Any): Any = {
        val zkClient = KafkaZkClient(config.getZookeeperAddr, false, 30000, 30000, Int.MaxValue, Time.SYSTEM)
        val adminZkClient = new AdminZkClient(zkClient)
        try {
            f(adminZkClient)
        } finally {
            zkClient.close()
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
