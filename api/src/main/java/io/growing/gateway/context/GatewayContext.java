package io.growing.gateway.context;

import io.growing.gateway.config.ConfigFactory;

public interface GatewayContext {

    String configPath();

    ConfigFactory configFactory();

}
