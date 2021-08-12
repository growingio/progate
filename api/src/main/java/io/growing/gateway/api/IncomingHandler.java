package io.growing.gateway.api;

import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpServerRequest;

import java.util.Set;

/**
 * @author AI
 */
public interface IncomingHandler {

    Set<HttpApi> apis();

    void handle(HttpServerRequest request);

}
