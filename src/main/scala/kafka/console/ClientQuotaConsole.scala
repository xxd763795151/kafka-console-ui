package kafka.console

import com.xuxd.kafka.console.config.KafkaConfig
import kafka.server.ConfigType
import org.apache.kafka.clients.admin.{Admin, AlterClientQuotasOptions}
import org.apache.kafka.common.quota.{ClientQuotaAlteration, ClientQuotaEntity, ClientQuotaFilter, ClientQuotaFilterComponent}

import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.{IterableHasAsJava, ListHasAsScala, SeqHasAsJava}

/**
 * client quota console.
 *
 * @author xuxd
 * @date 2022-12-30 10:55:56
 * */
class ClientQuotaConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {


  def getClientQuotasConfigs(entityTypes: java.util.List[String], entityNames: java.util.List[String]): java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]] = {
    withAdminClientAndCatchError(admin => getAllClientQuotasConfigs(admin, entityTypes.asScala.toList, entityNames.asScala.toList),
      e => {
        log.error("getAllClientQuotasConfigs error.", e)
        java.util.Collections.emptyMap()
      })
  }.asInstanceOf[java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]]]

  private def getAllClientQuotasConfigs(adminClient: Admin, entityTypes: List[String], entityNames: List[String]): java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]] = {
    val components = entityTypes.map(Some(_)).zipAll(entityNames.map(Some(_)), None, None).map { case (entityTypeOpt, entityNameOpt) =>
      val entityType = entityTypeOpt match {
        case Some(ConfigType.User) => ClientQuotaEntity.USER
        case Some(ConfigType.Client) => ClientQuotaEntity.CLIENT_ID
        case Some(ConfigType.Ip) => ClientQuotaEntity.IP
        case Some(_) => throw new IllegalArgumentException(s"Unexpected entity type ${entityTypeOpt.get}")
        case None => throw new IllegalArgumentException("More entity names specified than entity types")
      }
      entityNameOpt match {
        case Some("") => ClientQuotaFilterComponent.ofDefaultEntity(entityType)
        case Some(name) => ClientQuotaFilterComponent.ofEntity(entityType, name)
        case None => ClientQuotaFilterComponent.ofEntityType(entityType)
      }
    }

    adminClient.describeClientQuotas(ClientQuotaFilter.containsOnly(components.asJava)).entities.get(30, TimeUnit.SECONDS)
  }.asInstanceOf[java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]]]

  private def alterQuotaConfigs(adminClient: Admin, entityTypes: List[String], entityNames: List[String], configsToBeAddedMap: Map[String, String], configsToBeDeleted: Seq[String]) = {
    // handle altering client/user quota configs
    val oldConfig = getAllClientQuotasConfigs(adminClient, entityTypes, entityNames)

    val alterEntityTypes = entityTypes.map {
      case ConfigType.User => ClientQuotaEntity.USER
      case ConfigType.Client => ClientQuotaEntity.CLIENT_ID
      case ConfigType.Ip => ClientQuotaEntity.IP
      case entType => throw new IllegalArgumentException(s"Unexpected entity type: $entType")
    }
    val alterEntityNames = entityNames.map(en => if (en.nonEmpty) en else null)

    // Explicitly populate a HashMap to ensure nulls are recorded properly.
    val alterEntityMap = new java.util.HashMap[String, String]
    alterEntityTypes.zip(alterEntityNames).foreach { case (k, v) => alterEntityMap.put(k, v) }
    val entity = new ClientQuotaEntity(alterEntityMap)

    val alterOptions = new AlterClientQuotasOptions().validateOnly(false)
    val alterOps = (configsToBeAddedMap.map { case (key, value) =>
      val doubleValue = try value.toDouble catch {
        case _: NumberFormatException =>
          throw new IllegalArgumentException(s"Cannot parse quota configuration value for $key: $value")
      }
      new ClientQuotaAlteration.Op(key, doubleValue)
    } ++ configsToBeDeleted.map(key => new ClientQuotaAlteration.Op(key, null))).asJavaCollection

    adminClient.alterClientQuotas(Collections.singleton(new ClientQuotaAlteration(entity, alterOps)), alterOptions)
      .all().get(60, TimeUnit.SECONDS)
  }
}
