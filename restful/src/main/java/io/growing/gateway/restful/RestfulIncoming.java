package io.growing.gateway.restful;

import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

public class RestfulIncoming implements Incoming {

    @Override
    public Set<HttpApi> apis() {
        return null;
    }

    @Override
    public void handle(HttpServerRequest request) {

    }

    @Override
    public void reload(List<Upstream> upstreams, Set<Outgoing> outgoings) {

    }
}
