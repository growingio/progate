package io.growing.gateway.api;

import io.vertx.core.http.HttpServerRequest;

/**
 * @author AI
 */
public interface IncomingHandler {

    void handle(HttpServerRequest request);

}
