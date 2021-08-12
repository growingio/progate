package io.growing.gateway.restful.rule;

import io.growing.gateway.transcoder.Transcoder;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResponseRule extends HttpBodyRule {
    private HttpResponseStatus responseStatus;
    private ArrayList<String> fields;

    public ResponseRule(List<JsonPathTranscoder> transcoders) {
        super(transcoders);
    }
}