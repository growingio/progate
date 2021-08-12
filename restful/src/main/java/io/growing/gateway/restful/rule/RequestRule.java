package io.growing.gateway.restful.rule;

import java.util.List;

public class RequestRule extends HttpBodyRule {
    public RequestRule(List<JsonPathTranscoder> transcoders) {
        super(transcoders);
    }
}