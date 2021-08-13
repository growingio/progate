package io.growing.gateway.api;

import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public interface IncomingHandler {

    Set<HttpApi> apis();

    void handle(HttpServerRequest request);

    void reload(List<Upstream> upstreams, Set<OutgoingHandler> outgoings);

}
