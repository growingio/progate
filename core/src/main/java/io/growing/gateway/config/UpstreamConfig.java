package io.growing.gateway.config;

import io.growing.gateway.cluster.RoundRobin;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.meta.Upstream;

public class UpstreamConfig {

    private String name;
    private String protocol;
    private Node[] nodes;
    private String balancer;

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

    public Node[] getNodes() {
        return nodes;
    }

    public void setNodes(Node[] nodes) {
        this.nodes = nodes;
    }

    public static class Node {
        private String host;
        private int port;
        private int weight;

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

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        ServerNode toUpstreamNode() {
            final ServerNode node = new ServerNode();
            node.setHost(host);
            node.setPort(port);
            node.setWeight(weight);
            return node;
        }
    }

    public Upstream toUpstream() {
        final ServerNode[] serverNodes = new ServerNode[this.nodes.length];
        for (int i = 0; i < this.nodes.length; i++) {
            final UpstreamConfig.Node node = this.nodes[i];
            serverNodes[i] = node.toUpstreamNode();
        }
        final Upstream upstream = new Upstream();
        upstream.setName(name);
        upstream.setProtocol(protocol);
        upstream.setNodes(serverNodes);
        upstream.setBalancer(new RoundRobin());
        return upstream;
    }

}
