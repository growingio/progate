package io.growing.gateway.restful.rule.config;

import io.growing.gateway.context.GatewayContext;
import io.growing.gateway.restful.rule.RequestRule;

import java.util.Map;

public class RequestConfig {
    Map<String, String> transcoders;

    public void setTranscoders(Map<String, String> transcoders) {
        this.transcoders = transcoders;
    }

    public Map<String, String> getTranscoders() {
        return transcoders;
    }

    public RequestRule toRule(GatewayContext context) {
        return new RequestRule(ConfigUtils.makeJsonPathTranscoders(transcoders, context));
    }
}