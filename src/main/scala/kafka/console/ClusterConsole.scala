package kafka.console

import java.util.Collections
import java.util.concurrent.TimeUnit

import com.xuxd.kafka.console.beans.{BrokerNode, ClusterInfo}
import com.xuxd.kafka.console.config.KafkaConfig
import org.apache.kafka.clients.admin.DescribeClusterResult

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
            val clusterResult: DescribeClusterResult = admin.describeCluster()
            val clusterInfo = new ClusterInfo
            clusterInfo.setClusterId(clusterResult.clusterId().get(3000, TimeUnit.MILLISECONDS))
            val acls = clusterResult.authorizedOperations().get(3000, TimeUnit.MILLISECONDS)
            if (acls != null) {
                clusterInfo.setAuthorizedOperations(acls.asScala.map(_.toString).toSet[String].asJava)
            } else {
                clusterInfo.setAuthorizedOperations(Collections.emptySet())
            }
            clusterInfo.setNodes(clusterResult.nodes().get(3000, TimeUnit.MILLISECONDS).asScala.map(BrokerNode.fromNode(_)).toSet[BrokerNode].asJava)
            val id = clusterResult.controller().get(3000, TimeUnit.MILLISECONDS).id()
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
}
