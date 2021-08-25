package io.growing.gateway.restful.rule;

import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.ArrayList;
import java.util.List;

public class ResponseRule extends HttpBodyRule {
    private HttpResponseStatus responseStatus;
    private ArrayList<String> fields;

    public ResponseRule(List<JsonPathTranscoder> transcoders) {
        super(transcoders);
    }
}
