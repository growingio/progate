package io.growing.progate.restful;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outbound;
import io.growing.progate.http.Directive;
import io.growing.progate.restful.transcode.Coercing;
import io.growing.progate.restful.transcode.RestletTranscoder;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Restlet implements Handler<HttpServerRequest>, Directive {

    private static final Logger LOGGER = LoggerFactory.getLogger(Restlet.class);

    private final Operation operation;
    private final Outbound outbound;
    private final Upstream upstream;
    private final String endpoint;
    private final RestletTranscoder transcoder;

    private Restlet(Operation operation, Outbound outbound, Upstream upstream, String endpoint, RestletTranscoder transcoder) {
        this.outbound = outbound;
        this.upstream = upstream;
        this.endpoint = endpoint;
        this.operation = operation;
        this.transcoder = transcoder;
    }

    public static Restlet of(final OpenAPI openapi, Operation operation, Set<Outbound> outbounds, Upstream upstream, Map<String, Coercing> coercingSet) {
        Outbound outbound = null;
        for (Outbound ob : outbounds) {
            if ("grpc".equals(ob.protocol())) {
                outbound = ob;
            }
        }
        assert Objects.nonNull(outbound);
        final String endpoint = (String) operation.getExtensions().get("x-grpc-endpoint");
        final RestletTranscoder transcoder = new RestletTranscoder(openapi.getComponents(), coercingSet);
        return new Restlet(operation, outbound, upstream, endpoint, transcoder);
    }

    @Override
    public void handle(HttpServerRequest request) {
        if (Objects.nonNull(operation.getRequestBody())) {
            request.body(ar -> {
                if (ar.succeeded()) {
                    final JsonObject body = ar.result().toJsonObject();
                    sendRequest(request, body);
                } else {
                    //
                }
            });
        } else {
            sendRequest(request, null);
        }
    }

    private void sendRequest(final HttpServerRequest request, final JsonObject body) {
        final Map<String, Object> arguments = new HashMap<>();
        arguments.put("remoteAddress", getRemoteAddress(request));
        arguments.putAll(transcoder.parseParameters(request, operation.getParameters()));
        if (Objects.nonNull(body)) {
            arguments.putAll(readRequestBody(body));
        }
        final RequestContext context = new RestletRequestContext(getRequestId(request), arguments);
        outbound.handle(upstream, endpoint, context).thenApply(result -> {
            final MediaType mediaType = operation.getResponses().getDefault().getContent().get("application/json");
            return transcoder.serialize(result, mediaType);
        }).whenComplete((result, t) -> {
            if (Objects.nonNull(t)) {
                LOGGER.error(t.getLocalizedMessage(), t);
                request.response().setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                request.response().end(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase());
            } else {
                request.response().headers().add("Content-Type", "application/json;charset=utf-8");
                request.response().end(Json.encode(result));
            }
        });
    }

    private Map<String, Object> readRequestBody(final JsonObject body) {
        final MediaType mediaType = operation.getRequestBody().getContent().get("application/json");
        return transcoder.parseBody(body, mediaType);
    }


    private static class RestletRequestContext implements RequestContext {
        private final String id;
        private final Map<String, Object> arguments;

        public RestletRequestContext(String id, Map<String, Object> arguments) {
            this.id = id;
            this.arguments = arguments;
        }

        @Override
        public String id() {
            return this.id;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getArgument(String name) {
            return (T) this.arguments.get(name);
        }

        @Override
        public Map<String, Object> getArguments() {
            return this.arguments;
        }
    }

}
