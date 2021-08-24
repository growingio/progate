package io.growing.gateway.pipeline;

import io.growing.gateway.api.Upstream;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public interface Incoming {

    Set<HttpApi> apis();

    void handle(HttpServerRequest request);

    void reload(List<Upstream> upstreams, Set<Outgoing> outgoings);

}
