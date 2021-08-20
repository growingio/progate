package io.growing.gateway.config;

import io.growing.gateway.api.Upstream;
import io.growing.gateway.api.UpstreamNode;

public class UpstreamConfig {

    private String name;
    private String protocol;
    private Node[] nodes;

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

        UpstreamNode toUpstreamNode() {
            final UpstreamNode node = new UpstreamNode();
            node.setHost(host);
            node.setPort(port);
            node.setWeight(weight);
            return node;
        }
    }

    public Upstream toUpstream() {
        final UpstreamNode[] upstreamNodes = new UpstreamNode[this.nodes.length];
        for (int i = 0; i < this.nodes.length; i++) {
            final UpstreamConfig.Node node = this.nodes[i];
            upstreamNodes[i] = node.toUpstreamNode();
        }
        final Upstream upstream = new Upstream();
        upstream.setName(name);
        upstream.setProtocol(protocol);
        upstream.setNodes(upstreamNodes);
        return upstream;
    }

}
