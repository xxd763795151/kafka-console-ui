package kafka.console

import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.{Admin, AlterClientQuotasOptions}
import org.apache.kafka.common.quota.{ClientQuotaAlteration, ClientQuotaEntity, ClientQuotaFilter, ClientQuotaFilterComponent}

import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.{IterableHasAsJava, ListHasAsScala, MapHasAsScala, SeqHasAsJava}

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

  def addQuotaConfigs(entityTypes: java.util.List[String], entityNames: java.util.List[String], configsToBeAddedMap: java.util.Map[String, String]): (Boolean, String) = {
    alterQuotaConfigs(entityTypes, entityNames, configsToBeAddedMap, Collections.emptyList())
  }

  def deleteQuotaConfigs(entityTypes: java.util.List[String], entityNames: java.util.List[String], configsToBeDeleted: java.util.List[String]): (Boolean, String) = {
    alterQuotaConfigs(entityTypes, entityNames, Collections.emptyMap(), configsToBeDeleted)
  }

  def alterQuotaConfigs(entityTypes: java.util.List[String], entityNames: java.util.List[String], configsToBeAddedMap: java.util.Map[String, String], configsToBeDeleted: java.util.List[String]): (Boolean, String) = {
    withAdminClientAndCatchError(admin => alterQuotaConfigsInner(admin, entityTypes.asScala.toList, entityNames.asScala.toList, configsToBeAddedMap.asScala.toMap, configsToBeDeleted.asScala.toSeq),
      e => {
        log.error("getAllClientQuotasConfigs error.", e)
        (false, e.getMessage)
      })
    (true, "")
  }

  private def getAllClientQuotasConfigs(adminClient: Admin, entityTypes: List[String], entityNames: List[String]): java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]] = {
    val components = entityTypes.map(Some(_)).zipAll(entityNames.map(Some(_)), None, None).map { case (entityType, entityNameOpt) =>
      entityNameOpt match {
        case Some("") => ClientQuotaFilterComponent.ofDefaultEntity(entityType.get)
        case Some(name) => ClientQuotaFilterComponent.ofEntity(entityType.get, name)
        case None => ClientQuotaFilterComponent.ofEntityType(entityType.get)
      }
    }

    adminClient.describeClientQuotas(ClientQuotaFilter.containsOnly(components.asJava)).entities.get(30, TimeUnit.SECONDS)
  }.asInstanceOf[java.util.Map[ClientQuotaEntity, java.util.Map[String, Double]]]

  private def alterQuotaConfigsInner(adminClient: Admin, entityTypes: List[String], entityNames: List[String], configsToBeAddedMap: Map[String, String], configsToBeDeleted: Seq[String]) = {
    // handle altering client/user quota configs
    //    val oldConfig = getAllClientQuotasConfigs(adminClient, entityTypes, entityNames)
    //    val invalidConfigs = configsToBeDeleted.filterNot(oldConfig.asScala.toMap.contains)
    //    if (invalidConfigs.nonEmpty)
    //      throw new InvalidConfigurationException(s"Invalid config(s): ${invalidConfigs.mkString(",")}")

    val alterEntityNames = entityNames.map(en => if (en.nonEmpty) en else null)
    // Explicitly populate a HashMap to ensure nulls are recorded properly.
    val alterEntityMap = new java.util.HashMap[String, String]
    entityTypes.zip(alterEntityNames).foreach { case (k, v) => alterEntityMap.put(k, v) }
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
