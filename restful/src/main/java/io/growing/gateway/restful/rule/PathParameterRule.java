package io.growing.gateway.restful.rule;

import io.growing.gateway.transcoder.Transcoder;

import java.util.Map;

public class PathParameterRule {
    private Map<String, Transcoder> transcoders;

    public PathParameterRule(Map<String, Transcoder> transcoders) {
        this.transcoders = transcoders;
    }

    public Map<String, Transcoder> getTranscoders() {
        return transcoders;
    }

    public void setTranscoders(Map<String, Transcoder> transcoders) {
        this.transcoders = transcoders;
    }
}