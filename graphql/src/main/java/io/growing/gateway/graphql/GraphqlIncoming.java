package io.growing.gateway.graphql;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.DataFetcherExceptionHandler;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.graphql.handler.SimpleDataFetcherExceptionHandler;
import io.growing.gateway.graphql.idl.GraphqlBuilder;
import io.growing.gateway.graphql.request.GraphqlExecutionPayload;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.PluginArguments;
import io.growing.gateway.utilities.CollectionUtilities;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public class GraphqlIncoming implements Incoming {

    private final String contentType = "application/json;charset=utf-8";
    private final AtomicReference<GraphQL> graphQLReference = new AtomicReference<>();
    private final Logger logger = LoggerFactory.getLogger(GraphqlIncoming.class);
    private final Gson gson;
    private final GraphqlConfig config;
    private final DataFetcherExceptionHandler exceptionHandler;

    public GraphqlIncoming(GraphqlConfig config) {
        this.config = config;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.exceptionHandler = new SimpleDataFetcherExceptionHandler();
    }

    @Override
    public void reload(final List<ServiceMetadata> services, final Set<Outgoing> outgoings) {
        final GraphqlBuilder builder = GraphqlBuilder.newBuilder();
        graphQLReference.set(builder.outgoings(outgoings).services(services).exceptionHandler(exceptionHandler).build());
    }

    @Override
    public Set<HttpApi> apis() {
        final HttpApi httpApi = new HttpApi();
        String path = "/graphql";
        if (StringUtils.isNoneBlank(config.getPath())) {
            path = config.getPath();
        }
        httpApi.setPath(path);
        httpApi.setMethods(Sets.newHashSet(HttpMethod.POST));
        return Sets.newHashSet(httpApi);
    }

    @Override
    public void handle(HttpServerRequest request) {
        final GraphQL graphql = graphQLReference.get();
        if (Objects.isNull(graphql)) {
            endForError(request.response(), HttpResponseStatus.BAD_GATEWAY, new RuntimeException("Bad getaway"), gson);
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
                            response.headers().set(HttpHeaders.CONTENT_TYPE, contentType);
                            String chunk = gson.toJson(results);
                            response.end(chunk);
                        });
                    } else {
                        final GraphqlExecutionPayload payload = gson.fromJson(json, GraphqlExecutionPayload.class);
                        final ExecutionInput execution = buildExecution(request, payload);
                        final CompletableFuture<ExecutionResult> future = graphql.executeAsync(execution);
                        future.whenComplete((r, t) -> {
                            if (Objects.nonNull(t)) {
                                endForError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, t, gson);
                            } else {
                                HttpServerResponse response = request.response();
                                response.headers().set(HttpHeaders.CONTENT_TYPE, contentType);
                                String chunk = gson.toJson(r.toSpecification());
                                response.end(chunk);
                                if (CollectionUtilities.isNotEmpty(r.getErrors())) {
                                    r.getErrors().forEach(error -> {
                                        if (error instanceof Exception) {
                                            final Exception e = (Exception) error;
                                            logger.error(e.getLocalizedMessage(), e);
                                        }
                                    });
                                }
                            }
                        });
                    }
                } else {
                    endForError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, ar.cause(), gson);
                }
            } catch (Exception e) {
                endForError(request.response(), HttpResponseStatus.INTERNAL_SERVER_ERROR, e, gson);
            }
        });
    }

    private ExecutionInput buildExecution(final HttpServerRequest request, final GraphqlExecutionPayload payload) {
        final ExecutionInput.Builder builder = ExecutionInput.newExecutionInput();
        builder.query(payload.getQuery());
        if (Objects.nonNull(payload.getVariables())) {
            builder.variables(payload.getVariables());
        }
        if (StringUtils.isNotBlank(payload.getOperationName())) {
            builder.operationName(payload.getOperationName());
        }
        final Map<String, Object> arguments = new HashMap<>();
        arguments.putAll(new PluginArguments().arguments(request));
        arguments.put("request", request);
        arguments.put("payload", payload);
        builder.graphQLContext(arguments);
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private void endForError(final HttpServerResponse response, final HttpResponseStatus status, final Throwable cause, final Gson gson) {
        logger.error(cause.getLocalizedMessage(), cause);
        response.headers().set(HttpHeaders.CONTENT_TYPE, contentType);
        response.setStatusCode(status.code());
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        String message = cause.getLocalizedMessage();
        if (Objects.isNull(message)) {
            message = cause.getClass().getSimpleName();
        }
        builder.put("errors", Sets.newHashSet(ImmutableMap.of("message", message)));
        response.end(gson.toJson(builder.build()));
    }

}
