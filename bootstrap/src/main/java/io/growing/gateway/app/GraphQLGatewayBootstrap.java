package io.growing.gateway.app;

import com.google.common.collect.Sets;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.discovery.UpstreamDiscovery;
import io.growing.gateway.graphql.GraphqlIncoming;
import io.growing.gateway.grpc.GrpcOutgoing;
import io.growing.gateway.internal.ConfigUpstreamDiscovery;
import io.growing.gateway.pipeline.Outgoing;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
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

        final String configPath = getApplicationConfigFile();

        final UpstreamDiscovery discovery = new ConfigUpstreamDiscovery(configPath);
        final List<Upstream> upstreams = discovery.discover();
        final GraphqlIncoming incoming = new GraphqlIncoming();

        incoming.apis().forEach(api -> {
            api.getMethods().forEach(method -> {
                router.route(method, api.getPath()).handler(event -> incoming.handle(event.request()));
            });
        });
        final Set<Outgoing> outgoings = Sets.newHashSet(new GrpcOutgoing());

        router.get("/reload").handler(ctx -> {
            incoming.reload(upstreams, outgoings);
            ctx.response().end();
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

        final EventBus eventBus = vertx.eventBus();

        vertx.setPeriodic(1000, id -> {
            try {
                incoming.reload(upstreams, outgoings);
                eventBus.publish("timers.cancel", id);
            } catch (Exception e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        });

        eventBus.consumer("timers.cancel", message -> {
            long id = (long) message.body();
            vertx.cancelTimer(id);
        });

    }

    private static String getApplicationConfigFile() {
        return Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
    }

}
