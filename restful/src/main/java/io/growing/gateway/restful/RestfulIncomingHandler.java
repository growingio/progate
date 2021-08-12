package io.growing.gateway.restful;

import io.growing.gateway.api.IncomingHandler;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpServerRequest;

import java.util.Set;

public class RestfulIncomingHandler implements IncomingHandler {

    @Override
    public Set<HttpApi> apis() {
        return null;
    }

    @Override
    public void handle(HttpServerRequest request) {

    }
}
