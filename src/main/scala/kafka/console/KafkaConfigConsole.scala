package kafka.console

import com.xuxd.kafka.console.config.{ContextConfigHolder, KafkaConfig}
import kafka.server.ConfigType
import kafka.utils.Implicits.PropertiesOps
import org.apache.kafka.clients.admin._
import org.apache.kafka.common.config.SaslConfigs
import org.apache.kafka.common.security.scram.internals.{ScramCredentialUtils, ScramFormatter}

import java.security.MessageDigest
import java.util
import java.util.concurrent.TimeUnit
import java.util.{Properties, Set}
import scala.jdk.CollectionConverters.{CollectionHasAsScala, DictionaryHasAsScala, SeqHasAsJava}

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

    def addOrUpdateUser(name: String, pass: String): (Boolean, String) = {
        withAdminClient(adminClient => {
            try {
                val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
                val mechanisms = ContextConfigHolder.CONTEXT_CONFIG.get().getProperties().getProperty(SaslConfigs.SASL_MECHANISM).split(",").toSeq
                val scrams = mechanisms.map(m => new UserScramCredentialUpsertion(name,
                    new ScramCredentialInfo(ScramMechanism.fromMechanismName(m), defaultIterations), pass))
                adminClient.alterUserScramCredentials(scrams.asInstanceOf[Seq[UserScramCredentialAlteration]].asJava).all().get(timeoutMs, TimeUnit.MILLISECONDS)
                (true, "")
            } catch {
                case ex: Exception => log.error("addOrUpdateUser error", ex)
                    (false, ex.getMessage)
            }

        }).asInstanceOf[(Boolean, String)]
    }

    def addOrUpdateUserWithZK(name: String, pass: String): Boolean = {
        withZKClient(adminZkClient => {
            try {
                val credential = new ScramFormatter(org.apache.kafka.common.security.scram.internals.ScramMechanism.forMechanismName(ContextConfigHolder.CONTEXT_CONFIG.get().getProperties().getProperty(SaslConfigs.SASL_MECHANISM)))
                    .generateCredential(pass, defaultIterations)
                val credentialStr = ScramCredentialUtils.credentialToString(credential)

                val userConfig: Properties = new Properties()
                userConfig.put(ContextConfigHolder.CONTEXT_CONFIG.get().getProperties().getProperty(SaslConfigs.SASL_MECHANISM), credentialStr)

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

    /**
     * password consistency check.
     * return true: is consistent, or not.
     */
    def isPassConsistent(username: String, password: String): Boolean = {
        withZKClient(zkClient => {
            val entityConfig = zkClient.fetchEntityConfig(ConfigType.User, username)
            log.info(entityConfig.toString)
            var res: Boolean = false
            entityConfig.asScala.foreach(e => {
                val credential = ScramCredentialUtils.credentialFromString(e._2.asInstanceOf[String])
                val scramFormatter = new ScramFormatter(org.apache.kafka.common.security.scram.internals.ScramMechanism.forMechanismName(e._1.asInstanceOf[String]))
                val saltPassword = scramFormatter.saltedPassword(password, credential.salt(), credential.iterations())
                val expectStoredKey = credential.storedKey()
                val computedStoredKey = scramFormatter.storedKey(scramFormatter.clientKey(saltPassword))
                res |= MessageDigest.isEqual(expectStoredKey, computedStoredKey)
            })
            res
        }).asInstanceOf[Boolean]
    }

    def deleteUser(name: String): (Boolean, String) = {
        withAdminClient(adminClient => {
            try {
                //                adminClient.alterUserScramCredentials(util.Arrays.asList(
                //                    new UserScramCredentialDeletion(name, ScramMechanism.fromMechanismName(config.getSaslMechanism))))
                //                    .all().get(timeoutMs, TimeUnit.MILLISECONDS)
                // all delete
                val userDetail = getUserDetailList(util.Collections.singletonList(name))
                val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
                userDetail.values().asScala.foreach(u => {
                    adminClient.alterUserScramCredentials(u.credentialInfos().asScala.map(s => new UserScramCredentialDeletion(u.name(), s.mechanism())
                        .asInstanceOf[UserScramCredentialAlteration]).toList.asJava)
                        .all().get(timeoutMs, TimeUnit.MILLISECONDS)
                })

                (true, null)
            } catch {
                case ex: Exception => log.error("deleteUser error", ex)
                    (false, ex.getMessage)
            }

        }).asInstanceOf[(Boolean, String)]
    }
}
