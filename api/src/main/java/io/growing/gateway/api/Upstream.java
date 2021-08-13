package io.growing.gateway.api;

/**
 * @author AI
 */
public class Upstream {
    private String name;
    private String protocol;
    private UpstreamNode[] nodes;

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

    public UpstreamNode[] getNodes() {
        return nodes;
    }

    public void setNodes(UpstreamNode[] nodes) {
        this.nodes = nodes;
    }
}
