package io.growing.gateway.restful.rule;

import io.vertx.core.http.HttpMethod;

public class ApiRule {

    static class RequestRule {

    }

    static class PathParameterRule {

    }

    static class ResponseRule {

    }

    private HttpMethod method = null;
    private String path = null;
    private RequestRule requestRule = null;
    private PathParameterRule pathParameterRule = null;
    private ResponseRule responseRule = null;

    public ApiRule(HttpMethod method,
                   String path,
                   RequestRule requestRule,
                   PathParameterRule pathParameterRule,
                   ResponseRule responseRule) {
        this.method = method;
        this.path = path;
        this.requestRule = requestRule;
        this.pathParameterRule = pathParameterRule;
        this.responseRule = responseRule;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public RequestRule getRequestRule() {
        return requestRule;
    }

    public void setRequestRule(RequestRule requestRule) {
        this.requestRule = requestRule;
    }

    public PathParameterRule getPathParameterRule() {
        return pathParameterRule;
    }

    public void setPathParameterRule(PathParameterRule pathParameterRule) {
        this.pathParameterRule = pathParameterRule;
    }

    public ResponseRule getResponseRule() {
        return responseRule;
    }

    public void setResponseRule(ResponseRule responseRule) {
        this.responseRule = responseRule;
    }
}
