package kafka.console

import java.util
import java.util.Set
import java.util.concurrent.TimeUnit

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.{ScramCredentialInfo, ScramMechanism, UserScramCredentialDeletion, UserScramCredentialUpsertion}

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 11:29:48
 * */
class KafkaConfigConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    private val defaultIterations = 4096

    def getUserList(users: util.List[String]): Set[String] = {
        withAdminClient({
            adminClient => adminClient.describeUserScramCredentials(users).all().get().keySet()
        }).asInstanceOf[Set[String]]
    }

    def addOrUpdateUser(name: String, pass: String): Boolean = {
        withAdminClient(adminClient => {
            try {
                adminClient.alterUserScramCredentials(util.Arrays.asList(
                    new UserScramCredentialUpsertion(name,
                        new ScramCredentialInfo(ScramMechanism.fromMechanismName(config.getSaslMechanism), defaultIterations), pass)))
                    .all().get(3000, TimeUnit.MILLISECONDS)
                true
            } catch {
                case ex: Exception => log.error("addOrUpdateUser error", ex)
                    false
            }

        }).asInstanceOf[Boolean]
    }

    def deleteUser(name: String): Boolean = {
        withAdminClient(adminClient => {
            try {
                adminClient.alterUserScramCredentials(util.Arrays.asList(
                    new UserScramCredentialDeletion(name, ScramMechanism.fromMechanismName(config.getSaslMechanism))))
                    .all().get(3000, TimeUnit.MILLISECONDS)
                true
            } catch {
                case ex: Exception => log.error("deleteUser error", ex)
                    false
            }

        }).asInstanceOf[Boolean]
    }
}
