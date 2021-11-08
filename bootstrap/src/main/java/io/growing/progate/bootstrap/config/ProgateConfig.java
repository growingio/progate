package io.growing.progate.bootstrap.config;

import io.growing.gateway.config.OAuth2Config;
import io.growing.gateway.restful.config.RestfulConfig;

public class ProgateConfig {
    private ServerConfig server;
    private InboundConfig inbound;
    private RestfulConfig restful;
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

    public RestfulConfig getRestful() {
        return restful;
    }

    public void setRestful(RestfulConfig restful) {
        this.restful = restful;
    }

    public OAuth2Config getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2Config oauth2) {
        this.oauth2 = oauth2;
    }
}
