package io.growing.gateway.app;

import com.google.common.collect.Sets;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.discovery.UpstreamDiscovery;
import io.growing.gateway.graphql.GraphqlIncomingHandler;
import io.growing.gateway.grpc.GrpcOutgoingHandler;
import io.growing.gateway.internal.ConfigUpstreamDiscovery;
import io.vertx.core.Vertx;
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
        final GraphqlIncomingHandler incoming = new GraphqlIncomingHandler();

        incoming.apis().forEach(api -> {
            api.getMethods().forEach(method -> {
                router.route(method, api.getPath()).handler(event -> incoming.handle(event.request()));
            });
        });
        final Set<OutgoingHandler> outgoings = Sets.newHashSet(new GrpcOutgoingHandler());

        incoming.reload(upstreams, outgoings);

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

    private static String getApplicationConfigFile() {
        return Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
    }

}
