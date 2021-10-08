package com.xuxd.kafka.console.beans;

import org.apache.kafka.common.Node;

/**
 * kafka-console-ui.
 *
 * @author xuxd
 * @date 2021-10-08 14:03:21
 **/
public class BrokerNode {

    private int id;

    private String idString;

    private String host;

    private int port;

    private String rack;

    private boolean isController;

    public static BrokerNode fromNode(Node node) {
        BrokerNode brokerNode = new BrokerNode();
        brokerNode.setId(node.id());
        brokerNode.setIdString(node.idString());
        brokerNode.setHost(node.host());
        brokerNode.setPort(node.port());
        brokerNode.setRack(node.rack());

        return brokerNode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getRack() {
        return rack;
    }

    public void setRack(String rack) {
        this.rack = rack;
    }

    public boolean isController() {
        return isController;
    }

    public void setController(boolean controller) {
        isController = controller;
    }
}
