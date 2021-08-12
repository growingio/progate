package io.growing.gateway.restful.rule;

import io.growing.gateway.transcoder.Transcoder;

public class JsonPathTranscoder {

    private Transcoder underlying = null;
    private String jsonPath = null;

    public JsonPathTranscoder(String jsonPath, Transcoder underlying) {
        this.underlying = underlying;
        this.jsonPath = jsonPath;
    }


    public String getJsonPath() {
        return jsonPath;
    }

    public Transcoder getTranscoder() {
        return underlying;
    }
}
