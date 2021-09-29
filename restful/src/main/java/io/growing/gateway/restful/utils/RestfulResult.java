package io.growing.gateway.restful.utils;

import io.vertx.core.json.Json;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author zhuhongbin
 */
public class RestfulResult {
    private List<String> results;

    public RestfulResult() {
        this.results = new ArrayList<>();
    }

    public void add(String jsonText) {
        results.add(jsonText);
    }

    public List<String> asList() {
        return results;
    }

    public Object asJSON() {
        if (results.size() == 1) {
            return Json.encode(results.get(0));
        }
        return results.stream().map(Json::encode).collect(toList());
    }
}