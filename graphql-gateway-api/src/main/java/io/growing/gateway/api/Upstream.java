package io.growing.gateway.api;

/**
 * @author AI
 */
public class Upstream {
    private String name;
    private UpstreamNode[] nodes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UpstreamNode[] getNodes() {
        return nodes;
    }

    public void setNodes(UpstreamNode[] nodes) {
        this.nodes = nodes;
    }

}
