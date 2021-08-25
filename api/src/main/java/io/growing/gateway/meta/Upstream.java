package io.growing.gateway.meta;

import io.growing.gateway.cluster.LoadBalance;

/**
 * @author AI
 */
public class Upstream {
    private String name;
    private String protocol;
    private ServerNode[] nodes;
    private LoadBalance balancer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public ServerNode[] getNodes() {
        return nodes;
    }

    public void setNodes(ServerNode[] nodes) {
        this.nodes = nodes;
    }

    public LoadBalance getBalancer() {
        return balancer;
    }

    public void setBalancer(LoadBalance balancer) {
        this.balancer = balancer;
    }

}
