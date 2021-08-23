package io.growing.gateway.context;

import io.growing.gateway.config.ConfigFactory;

public class DefaultGatewayContext implements GatewayContext {

    private final String configPath;

    public DefaultGatewayContext(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String configPath() {
        return this.configPath;
    }

    @Override
    public ConfigFactory configFactory() {
        return null;
    }
}
