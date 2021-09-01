package kafka.console

import java.util
import java.util.concurrent.TimeUnit
import java.util.{Collections, List}

import com.xuxd.kafka.console.beans.AclEntry
import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.commons.lang3.StringUtils
import org.apache.kafka.common.acl._
import org.apache.kafka.common.resource.{ResourcePattern, ResourcePatternFilter, ResourceType}
import org.apache.kafka.common.security.auth.KafkaPrincipal

import scala.jdk.CollectionConverters.SetHasAsJava

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-08-28 19:53:06
 * */
class KafkaAclConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    def getAclList(entry: AclEntry): List[AclBinding] = {

        if (entry == null) {
            withAdminClient(adminClient => adminClient.describeAcls(AclBindingFilter.ANY).values().get()).asInstanceOf[List[AclBinding]]
        } else {
            entry.isNull match {
                case true => withAdminClient(adminClient => adminClient.describeAcls(AclBindingFilter.ANY).values().get()).asInstanceOf[List[AclBinding]]
                case false => {
                    val f = entry.toAclBindingFilter
                    var resourceType: ResourceType = ResourceType.ANY
                    if (f.patternFilter().resourceType() != ResourceType.UNKNOWN) {
                        resourceType = f.patternFilter().resourceType()
                    }

                    var name: String = null
                    if (f.patternFilter().name() != ResourcePattern.WILDCARD_RESOURCE) {
                        name = f.patternFilter().name()
                    }

                    var principal: String = null
                    if (StringUtils.isNotBlank(entry.getPrincipal()) && !KafkaPrincipal.ANONYMOUS.toString.equalsIgnoreCase(f.entryFilter().principal())) {
                        principal = f.entryFilter().principal();
                    }
                    val filter = new AclBindingFilter(new ResourcePatternFilter(resourceType, name, f.patternFilter().patternType()),
                        new AccessControlEntryFilter(principal, f.entryFilter().host(), AclOperation.ANY, AclPermissionType.ANY))
                    log.info(filter.toString)
                    withAdminClient(adminClient => adminClient.describeAcls(filter).values().get()).asInstanceOf[List[AclBinding]]
                }
            }
        }
    }

    def addAcl(acls: List[AclBinding]): Boolean = {
        withAdminClient(adminClient => {
            try {
                adminClient.createAcls(acls).all().get(3000, TimeUnit.MILLISECONDS)
                true
            } catch {
                case e: Exception => log.error("addAcl error", e)
                    false
            }
        }).asInstanceOf[Boolean]
    }

    // must param: entry.topic, entry.user
    def addProducerAcl(entry: AclEntry): Boolean = {
        // topic
        // user
        val param = entry.toAclBinding

        val binding = new AclBinding(new ResourcePattern(ResourceType.TOPIC, param.pattern().name(), param.pattern().patternType()),
            new AccessControlEntry(param.entry().principal(), param.entry().host(), param.entry().operation(), AclPermissionType.ALLOW))

        addAcl(new util.ArrayList[AclBinding](getAclBindings(binding, Set(AclOperation.CREATE, AclOperation.DESCRIBE, AclOperation.WRITE)).asJava))
    }

    def addConsumerAcl(topicEntry: AclEntry, groupEntry: AclEntry): Boolean = {
        // topic
        // group
        // user
        val tp = topicEntry.toAclBinding
        val gp = groupEntry.toAclBinding

        val bindingTopic = new AclBinding(new ResourcePattern(ResourceType.TOPIC, tp.pattern().name(), tp.pattern().patternType()),
            new AccessControlEntry(tp.entry().principal(), tp.entry().host(), tp.entry().operation(), AclPermissionType.ALLOW))

        val bindingGroup = new AclBinding(new ResourcePattern(ResourceType.GROUP, gp.pattern().name(), gp.pattern().patternType()),
            new AccessControlEntry(gp.entry().principal(), gp.entry().host(), gp.entry().operation(), AclPermissionType.ALLOW))

        val acls: Set[AclBinding] = getAclBindings(bindingTopic, Set(AclOperation.READ)) ++ getAclBindings(bindingGroup, Set(AclOperation.READ))

        addAcl(new util.ArrayList[AclBinding](acls.asJava))
    }

    def deleteAcl(entry: AclEntry, allResource: Boolean, allPrincipal: Boolean, allOperation: Boolean): Boolean = {
        withAdminClient(adminClient => {
            try {
                val result = adminClient.deleteAcls(Collections.singleton(entry.toAclBindingFilter(allResource, allPrincipal, allOperation))).all().get(3000, TimeUnit.MILLISECONDS)
                log.info("delete acl: {}", result)
                true
            } catch {
                case e: Exception => log.error("addAcl error", e)
                    false
            }
        }).asInstanceOf[Boolean]
    }

    def deleteAcl(filters: util.Collection[AclBindingFilter]): Boolean = {
        withAdminClient(adminClient => {
            try {
                val result = adminClient.deleteAcls(filters).all().get(3000, TimeUnit.MILLISECONDS)
                log.info("delete acl: {}", result)
                true
            } catch {
                case e: Exception => log.error("deleteAcl error", e)
                    false
            }
        }).asInstanceOf[Boolean]
    }

    def deleteUserAcl(entry: AclEntry): Boolean = {
        val filter: AclBindingFilter = entry.toAclBindingFilter
        val delFilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.ANY, ResourcePattern.WILDCARD_RESOURCE, filter.patternFilter().patternType()),
            new AccessControlEntryFilter(filter.entryFilter().principal(), filter.entryFilter().host(), AclOperation.ANY, AclPermissionType.ANY))

        deleteAcl(Collections.singleton(delFilter))
    }

    def deleteProducerAcl(entry: AclEntry): Boolean = {
        val filter: AclBindingFilter = entry.toAclBindingFilter
        val delFilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.TOPIC, filter.patternFilter().name(), filter.patternFilter().patternType()),
            new AccessControlEntryFilter(filter.entryFilter().principal(), filter.entryFilter().host(), AclOperation.ANY, AclPermissionType.ANY))

        deleteAcl(getAclFilters(delFilter, Set(AclOperation.CREATE, AclOperation.DESCRIBE, AclOperation.WRITE)).asJava)
    }

    def deleteConsumerAcl(topic: AclEntry, group: AclEntry): Boolean = {
        val (topicFilter, groupFilter) = (topic.toAclBindingFilter(), group.toAclBindingFilter())
        val delTopicFilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.TOPIC, topicFilter.patternFilter().name(), topicFilter.patternFilter().patternType()),
            new AccessControlEntryFilter(topicFilter.entryFilter().principal(), topicFilter.entryFilter().host(), AclOperation.ANY, AclPermissionType.ANY))
        val delGroupFilter = new AclBindingFilter(new ResourcePatternFilter(ResourceType.GROUP, groupFilter.patternFilter().name(), groupFilter.patternFilter().patternType()),
            new AccessControlEntryFilter(groupFilter.entryFilter().principal(), groupFilter.entryFilter().host(), AclOperation.ANY, AclPermissionType.ANY))

        val filters = getAclFilters(delTopicFilter, Set(AclOperation.READ)) ++ getAclFilters(delGroupFilter, Set(AclOperation.READ))
        deleteAcl(filters.asJava)
    }

    private def getAclBindings(binding: AclBinding, ops: Set[AclOperation]): Set[AclBinding] = {
        for {
            op <- ops
        } yield {
            new AclBinding(new ResourcePattern(binding.pattern().resourceType(), binding.pattern().name(), binding.pattern().patternType()),
                new AccessControlEntry(binding.entry().principal(), binding.entry().host(), op, binding.entry().permissionType()))
        }
    }

    private def getAclFilters(filter: AclBindingFilter, ops: Set[AclOperation]): Set[AclBindingFilter] = {
        ops.map(o => new AclBindingFilter(new ResourcePatternFilter(filter.patternFilter().resourceType(), filter.patternFilter().name(), filter.patternFilter().patternType()),
            new AccessControlEntryFilter(filter.entryFilter().principal(), filter.entryFilter().host(), o, filter.entryFilter().permissionType())))
    }

}
