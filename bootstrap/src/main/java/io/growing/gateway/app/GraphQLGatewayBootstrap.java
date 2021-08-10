package io.growing.gateway.app;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;

/**
 * @author AI
 */
public class GraphQLGatewayBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(GraphQLGatewayBootstrap.class);

    public static void main(final String[] args) {
        final Vertx vertx = Vertx.vertx();
        final HttpServer server = vertx.createHttpServer();
        final Router router = Router.router(vertx);
        router.route().handler(ctx -> {
            ctx.response().end("Hello");
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            final CountDownLatch counter = new CountDownLatch(1);
            logger.info("Server on shutdown...");
            server.close(ar ->
                vertx.close().onSuccess(handler -> {
                    logger.info("Server shutdown success");
                    counter.countDown();
                }));
            try {
                counter.await();
            } catch (InterruptedException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }));

        server.requestHandler(router).listen(8080).onSuccess(handler -> logger.info("Server listening on 8080"));
    }

}
