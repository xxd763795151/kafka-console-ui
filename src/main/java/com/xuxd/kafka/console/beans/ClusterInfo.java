package com.xuxd.kafka.console.beans;

import java.util.Set;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 13:57:48
 **/
public class ClusterInfo {

    private Set<BrokerNode> nodes;

    private Set<String> authorizedOperations;

    private String clusterId;

    public Set<BrokerNode> getNodes() {
        return nodes;
    }

    public void setNodes(Set<BrokerNode> nodes) {
        this.nodes = nodes;
    }

    public Set<String> getAuthorizedOperations() {
        return authorizedOperations;
    }

    public void setAuthorizedOperations(Set<String> authorizedOperations) {
        this.authorizedOperations = authorizedOperations;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
