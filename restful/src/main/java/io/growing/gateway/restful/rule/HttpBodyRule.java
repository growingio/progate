package io.growing.gateway.restful.rule;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.MapFunction;
import io.growing.gateway.transcoder.Transcoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class HttpBodyRule {
    private List<JsonPathTranscoder> transcoders;

    public HttpBodyRule(List<JsonPathTranscoder> transcoders) {
        this.transcoders = transcoders;
    }

    public void AddTranscoder(String jsonPath, Transcoder transcoder) {
        if (Objects.isNull(transcoders)) {
            transcoders = new ArrayList<>();
        }
        transcoders.add(new JsonPathTranscoder(jsonPath, transcoder));
    }

    public Object transcodeBody(Object body) {
        DocumentContext result = JsonPath.parse(body);
        for (JsonPathTranscoder transcoder : transcoders) {
            MapFunction mapFunction = (currentValue, configuration) -> transcoder.getTranscoder().transcode(currentValue);
            result = result.map(transcoder.getJsonPath(), mapFunction);
        }
        return result;
    }
}
