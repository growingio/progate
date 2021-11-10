package io.growing.gateway.graphql;

import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.graphql.handler.GraphqlEndpointHandler;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.HttpEndpoint;
import io.growing.gateway.pipeline.Inbound;
import io.growing.gateway.pipeline.Outbound;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public class GraphqlInbound implements Inbound {

    private final GraphqlConfig config;

    @Inject
    public GraphqlInbound(GraphqlConfig config) {
        this.config = config;
    }

    @Override
    public Set<HttpEndpoint> endpoints(List<ServiceMetadata> services, Set<Outbound> outbounds, RuntimeContext context) {
        final String path = StringUtils.isNoneBlank(config.getPath()) ? config.getPath() : "/graphql";
        final HttpEndpoint endpoint = new HttpEndpoint();
        endpoint.setPath(path);
        endpoint.setMethods(Set.of(HttpMethod.POST));
        final Handler<HttpServerRequest> handler = new GraphqlEndpointHandler(config, services, outbounds, context);
        endpoint.setHandler(handler);
        return Set.of(endpoint);
    }

}
