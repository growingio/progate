package io.growing.gateway.restful.rule.config;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.restful.rule.ResponseRule;

import java.util.Map;

public class ResponseConfig {
    Map<String, String> transcoders;

    String[] fields;

    public void setFields(String[] fields) {
        this.fields = fields;
    }

    public String[] getFields() {
        return fields;
    }

    public Map<String, String> getTranscoders() {
        return transcoders;
    }

    public void setTranscoders(Map<String, String> transcoders) {
        this.transcoders = transcoders;
    }

    public ResponseRule toRule(RequestContext context) {
        return null;
    }
}
