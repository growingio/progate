package io.growing.gateway.graphql;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import io.growing.gateway.api.IncomingHandler;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.graphql.idl.GraphqlBuilder;
import io.growing.gateway.graphql.request.GraphqlRelayRequest;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author AI
 */
public class GraphqlIncomingHandler implements IncomingHandler {

    private final AtomicReference<GraphQL> graphQLReference = new AtomicReference<>();

    @Override
    public void reload(final List<Upstream> upstreams, final Set<OutgoingHandler> outgoings) {
        final GraphqlBuilder builder = GraphqlBuilder.newBuilder();
        builder.outgoings(outgoings).upstreams(upstreams);
        graphQLReference.set(builder.build());
    }

    @Override
    public Set<HttpApi> apis() {
        final HttpApi httpApi = new HttpApi();
        httpApi.setPath("/graphql");
        httpApi.setMethods(Sets.newHashSet(HttpMethod.POST));
        return Sets.newHashSet(httpApi);
    }

    @Override
    public void handle(HttpServerRequest request) {
        request.body(ar -> {
            if (ar.succeeded()) {
                final Gson gson = new Gson();
                //
                final GraphqlRelayRequest graphqlRequest = gson.fromJson(ar.result().toString(StandardCharsets.UTF_8), GraphqlRelayRequest.class);
                final GraphQLContext context = GraphQLContext.newContext().build();
                final ExecutionInput execution = ExecutionInput.newExecutionInput(graphqlRequest.getQuery()).localContext(context).build();
                final CompletableFuture<ExecutionResult> future = graphQLReference.get().executeAsync(execution);
                //
                future.whenComplete((r, t) -> {
                    if (Objects.nonNull(t)) {
                        request.response().setStatusCode(500).end("Error");
                    } else {
                        HttpServerResponse response = request.response();
                        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                        try {
                            String chunk = gson.toJson(r);
                            response.end(chunk);
                        } catch (Exception e) {
                            request.response().setStatusCode(500).end("Error");
                        }
                    }
                });
            } else {
                request.response().setStatusCode(500).end("Error");
            }
        });
    }

}
