package io.growing.progate.bootstrap;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class HealthyCheck implements Handler<RoutingContext> {

    public final String path = "/healthy-check";

    @Override
    public void handle(RoutingContext event) {
        event.response().end("imok");
    }

}
