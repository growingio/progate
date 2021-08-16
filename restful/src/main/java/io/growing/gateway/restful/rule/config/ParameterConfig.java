package io.growing.gateway.restful.rule.config;

import io.growing.gateway.context.GatewayContext;
import io.growing.gateway.restful.rule.PathParameterRule;

import java.util.Map;

public class ParameterConfig {
    Map<String, String> transcoders;

    public Map<String, String> getTranscoders() {
        return transcoders;
    }

    public void setTranscoders(Map<String, String> transcoders) {
        this.transcoders = transcoders;
    }

    public PathParameterRule toRule(GatewayContext context) {
        return new PathParameterRule(ConfigUtils.makeTranscoders(transcoders, context));
    }
}
