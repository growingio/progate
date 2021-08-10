package io.growing.gateway;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;

/**
 * @author AI
 */
public interface HttpRequestInterceptor {

    void access(Vertx vertx, HttpServerRequest request);


}
