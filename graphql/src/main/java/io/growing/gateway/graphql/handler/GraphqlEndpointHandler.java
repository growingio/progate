package io.growing.gateway.graphql.handler;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.ExecutionId;
import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.graphql.idl.GraphqlBuilder;
import io.growing.gateway.graphql.plugin.GraphqlInboundPlugin;
import io.growing.gateway.graphql.request.GraphqlExecutionPayload;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outbound;
import io.growing.gateway.utilities.CollectionUtilities;
import io.growing.progate.http.Directive;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class GraphqlEndpointHandler implements Handler<HttpServerRequest>, Directive {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphqlEndpointHandler.class);
    private final Gson gson;
    private final GraphQL graphql;
    private final List<GraphqlInboundPlugin> plugins;

    public GraphqlEndpointHandler(GraphqlConfig config,
                                  List<ServiceMetadata> services,
                                  Set<Outbound> outbounds, RuntimeContext context) {
        this.gson = new GsonBuilder().serializeNulls().create();
        this.plugins = Objects.isNull(config.getPlugins()) ? Collections.emptyList() : config.getPlugins().stream()
            .map(pluginName -> {
                final GraphqlInboundPlugin plugin = context.createPlugin(pluginName);
                plugin.init(config, context);
                return plugin;
            }).collect(Collectors.toList());
        final GraphqlBuilder builder = GraphqlBuilder.newBuilder();
        final DataFetcherExceptionHandler exceptionHandler = new SimpleDataFetcherExceptionHandler();
        this.graphql = builder.outgoings(outbounds)
            .services(services).plugins(plugins).exceptionHandler(exceptionHandler).build();
    }


    @Override
    public void handle(HttpServerRequest request) {
        if (Objects.isNull(graphql)) {
            endError(request.response(), HttpResponseStatus.BAD_GATEWAY, new RuntimeException("Bad getaway"), gson);
            return;
        }
        request.body(ar -> {
            try {
                if (ar.succeeded()) {
                    final JsonElement json = gson.fromJson(ar.result().toString(StandardCharsets.UTF_8), JsonElement.class);
                    if (json.isJsonArray()) {
                        final GraphqlExecutionPayload[] payloads = gson.fromJson(json, GraphqlExecutionPayload[].class);
                        final List<CompletableFuture<ExecutionResult>> futures = new ArrayList<>(payloads.length);
                        for (GraphqlExecutionPayload payload : payloads) {
                            final ExecutionInput execution = buildExecution(request, payload);
                            futures.add(graphql.executeAsync(execution));
                        }
                        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v -> {
                            return futures.stream().map(CompletableFuture::join)
                                .map(ExecutionResult::toSpecification).collect(Collectors.toList());
                        }).whenComplete((results, t) -> {
                            HttpServerResponse response = request.response();
                            asJsonContentType(response);
                            String chunk = gson.toJson(results);
                            response.end(chunk);
                        });
                    } else {
                        final GraphqlExecutionPayload payload = gson.fromJson(json, GraphqlExecutionPayload.class);
                        final ExecutionInput execution = buildExecution(request, payload);
                        final CompletableFuture<ExecutionResult> future = graphql.executeAsync(execution);
                        future.whenComplete((r, t) -> {
                            if (Objects.nonNull(t)) {
                                endError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, t, gson);
                            } else {
                                HttpServerResponse response = request.response();
                                asJsonContentType(response);
                                String chunk = gson.toJson(r.toSpecification());
                                response.end(chunk);
                                if (CollectionUtilities.isNotEmpty(r.getErrors())) {
                                    r.getErrors().forEach(error -> {
                                        if (error instanceof Exception) {
                                            final Exception e = (Exception) error;
                                            LOGGER.error(e.getLocalizedMessage(), e);
                                        }
                                    });
                                }
                            }
                        });
                    }
                } else {
                    endError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, ar.cause(), gson);
                }
            } catch (Exception e) {
                endError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, e, gson);
            }
        });
    }

    private ExecutionInput buildExecution(final HttpServerRequest request, final GraphqlExecutionPayload payload) {
        final ExecutionId id = ExecutionId.from(getRequestId(request));
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput().executionId(id);
        builder.query(payload.getQuery());
        if (Objects.nonNull(payload.getVariables())) {
            builder.variables(payload.getVariables());
        }
        if (StringUtils.isNotBlank(payload.getOperationName())) {
            builder.operationName(payload.getOperationName());
        }
        final Map<String, Object> arguments = new HashMap<>();
        if (!plugins.isEmpty()) {
            plugins.forEach(plugin -> arguments.putAll(plugin.arguments(request)));
        }
        arguments.put("request", request);
        arguments.put("payload", payload);
        arguments.put("remoteAddress", getRemoteAddress(request));
        builder.graphQLContext(arguments);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private void endError(final HttpServerResponse response, final HttpResponseStatus status, final Throwable cause, final Gson gson) {
        LOGGER.error(cause.getLocalizedMessage(), cause);
        asJsonContentType(response);
        response.setStatusCode(status.code());
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        String message = cause.getLocalizedMessage();
        if (Objects.isNull(message)) {
            message = cause.getClass().getSimpleName();
        }
        builder.put("errors", Sets.newHashSet(Map.of("message", message)));
        response.end(gson.toJson(builder.build()));
    }

}
