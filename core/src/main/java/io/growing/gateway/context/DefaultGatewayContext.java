package io.growing.gateway.context;

public class DefaultGatewayContext implements GatewayContext {

    private final String configPath;

    public DefaultGatewayContext(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public String configPath() {
        return this.configPath;
    }

}
