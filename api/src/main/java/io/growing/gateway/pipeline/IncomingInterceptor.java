package io.growing.gateway.pipeline;

import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

public interface IncomingInterceptor {

    void access(HttpServerRequest request);

    void headerFilter(HttpServerResponse response);

    void bodyFilter(HttpServerResponse response);

    void log(HttpServerRequest request, HttpServerResponse response);

}
