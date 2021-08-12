package io.growing.gateway.restful.rule.config;

import io.growing.gateway.context.GatewayContext;
import io.growing.gateway.restful.rule.JsonPathTranscoder;
import io.growing.gateway.transcoder.Transcoder;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUtils {

    static List<JsonPathTranscoder> makeJsonPathTranscoders(Map<String, String> transcoders, GatewayContext context) {
        return transcoders.entrySet().stream().map(entry -> {
            Transcoder transcoder = context.getTranscoder(entry.getValue()).orElseGet(() -> Transcoder.empty);
            return new JsonPathTranscoder(entry.getKey(),transcoder);
        }).collect(Collectors.toList());
    }

    static Map<String, Transcoder> makeTranscoders(Map<String, String> transcoders, GatewayContext context) {
        return transcoders.entrySet().stream().map(entry -> {
            Transcoder transcoder = context.getTranscoder(entry.getValue()).orElseGet(() -> Transcoder.empty);
            return new AbstractMap.SimpleEntry<>(entry.getKey(), transcoder);
        }).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
}
