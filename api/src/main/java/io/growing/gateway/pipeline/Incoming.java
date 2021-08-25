package io.growing.gateway.pipeline;

import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public interface Incoming {

    Set<HttpApi> apis();

    void handle(HttpServerRequest request);

    void reload(List<ServiceMetadata> services, Set<Outgoing> outgoings);

}
