package io.growing.gateway.pipeline;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.util.Set;

public final class HttpEndpoint {

    private String path;
    private Set<HttpMethod> methods;
    private Handler<HttpServerRequest> handler;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(Set<HttpMethod> methods) {
        this.methods = methods;
    }

    public Handler<HttpServerRequest> getHandler() {
        return handler;
    }

    public void setHandler(Handler<HttpServerRequest> handler) {
        this.handler = handler;
    }
}
