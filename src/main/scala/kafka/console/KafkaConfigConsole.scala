package kafka.console

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Properties, Set}

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.server.ConfigType
import kafka.utils.Implicits.PropertiesOps
import org.apache.kafka.clients.admin._
import org.apache.kafka.common.security.scram.internals.{ScramCredentialUtils, ScramFormatter}

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

    def getUserDetailList(users: util.List[String]): util.Map[String, UserScramCredentialsDescription] = {
        withAdminClient({
            adminClient => adminClient.describeUserScramCredentials(users).all().get()
        }).asInstanceOf[util.Map[String, UserScramCredentialsDescription]]
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

    def addOrUpdateUserWithZK(name: String, pass: String): Boolean = {
        withZKClient(adminZkClient => {
            try {
                val credential = new ScramFormatter(org.apache.kafka.common.security.scram.internals.ScramMechanism.forMechanismName(config.getSaslMechanism))
                    .generateCredential(pass, defaultIterations)
                val credentialStr = ScramCredentialUtils.credentialToString(credential)

                val userConfig: Properties = new Properties()
                userConfig.put(config.getSaslMechanism, credentialStr)

                val configs = adminZkClient.fetchEntityConfig(ConfigType.User, name)
                userConfig ++= configs
                adminZkClient.changeConfigs(ConfigType.User, name, userConfig)
                true
            } catch {
                case e: Exception => log.error("addOrUpdateAdminWithZK error.", e)
                    false
            }
        }).asInstanceOf[Boolean]
    }

    def deleteUser(name: String): (Boolean, String) = {
        withAdminClient(adminClient => {
            try {
                adminClient.alterUserScramCredentials(util.Arrays.asList(
                    new UserScramCredentialDeletion(name, ScramMechanism.fromMechanismName(config.getSaslMechanism))))
                    .all().get(3000, TimeUnit.MILLISECONDS)
                (true, null)
            } catch {
                case ex: Exception => log.error("deleteUser error", ex)
                    (false, ex.getMessage)
            }

        }).asInstanceOf[(Boolean, String)]
    }
}
