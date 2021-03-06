package kafka.console

import com.xuxd.kafka.console.beans.{BrokerNode, ClusterInfo}
import com.xuxd.kafka.console.config.{ContextConfigHolder, KafkaConfig}
import org.apache.kafka.clients.NodeApiVersions
import org.apache.kafka.clients.admin.DescribeClusterResult
import org.apache.kafka.common.Node

import java.util.Collections
import java.util.concurrent.TimeUnit
import scala.jdk.CollectionConverters.{CollectionHasAsScala, SetHasAsJava, SetHasAsScala}

/**
 * kafka-console-ui. cluster console.
 *
 * @author xuxd
 * @date 2021-10-08 10:55:56
 * */
class ClusterConsole(config: KafkaConfig) extends KafkaConsole(config: KafkaConfig) with Logging {

    def clusterInfo(): ClusterInfo = {
        withAdminClientAndCatchError(admin => {
            val timeoutMs = ContextConfigHolder.CONTEXT_CONFIG.get().getRequestTimeoutMs()
            val clusterResult: DescribeClusterResult = admin.describeCluster()
            val clusterInfo = new ClusterInfo
            clusterInfo.setClusterId(clusterResult.clusterId().get(timeoutMs, TimeUnit.MILLISECONDS))
            val acls = clusterResult.authorizedOperations().get(timeoutMs, TimeUnit.MILLISECONDS)
            if (acls != null) {
                clusterInfo.setAuthorizedOperations(acls.asScala.map(_.toString).toSet[String].asJava)
            } else {
                clusterInfo.setAuthorizedOperations(Collections.emptySet())
            }
            clusterInfo.setNodes(clusterResult.nodes().get(timeoutMs, TimeUnit.MILLISECONDS).asScala.map(BrokerNode.fromNode(_)).toSet[BrokerNode].asJava)
            val id = clusterResult.controller().get(timeoutMs, TimeUnit.MILLISECONDS).id()
            clusterInfo.getNodes.asScala.foreach(n => {
                if (n.getId == id) {
                    n.setController(true)
                }
            })
            clusterInfo
        }, eh => {
            log.error("get clusterInfo error.", eh)
            new ClusterInfo
        }).asInstanceOf[ClusterInfo]
    }

    def listBrokerVersionInfo(): java.util.HashMap[Node, NodeApiVersions] = {
        BrokerApiVersion.listAllBrokerApiVersionInfo()
    }
}
