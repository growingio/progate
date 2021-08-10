package io.growing.gateway.api;

import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpServerRequest;

import java.util.Optional;

/**
 * @author AI
 */
public interface IncomingHandler {

    Optional<HttpApi> api();

    void handle(HttpServerRequest request);

}
