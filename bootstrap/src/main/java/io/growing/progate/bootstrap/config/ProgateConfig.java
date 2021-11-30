package io.growing.progate.bootstrap.config;

import io.growing.gateway.config.OAuth2Config;

public class ProgateConfig {
    private ServerConfig server;
    private InboundConfig inbound;
    private OAuth2Config oauth2;

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

    public OAuth2Config getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2Config oauth2) {
        this.oauth2 = oauth2;
    }
}
