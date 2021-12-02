package io.growing.progate.bootstrap.config;

public class ProgateConfig {
    private ServerConfig server;
    private InboundConfig inbound;

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public InboundConfig getInbound() {
        return inbound;
    }

    public void setInbound(InboundConfig inbound) {
        this.inbound = inbound;
    }

}
