package io.growing.gateway.pipeline;

import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public interface Inbound {

    Set<HttpApi> apis(List<ServiceMetadata> services);

    void handle(HttpServerRequest request);

    void handle(HttpApi httpApi, HttpServerRequest request);

    void reload(List<ServiceMetadata> services, Set<Outgoing> outgoings, RuntimeContext context);

}
