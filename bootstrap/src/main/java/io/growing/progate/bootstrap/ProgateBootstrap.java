package io.growing.progate.bootstrap;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.discovery.ServiceDiscoveryService;
import io.growing.gateway.grpc.GrpcOutgoing;
import io.growing.gateway.grpc.discovery.GrpcReflectionServiceDiscovery;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.progate.bootstrap.config.ProgateConfig;
import io.growing.progate.bootstrap.di.ProgateModule;
import io.growing.progate.bootstrap.loader.InboundLoader;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author AI
 */
public class ProgateBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ProgateBootstrap.class);

    public static void main(final String[] args) throws Exception {
        final String configPath = ProgateModule.getApplicationConfigFile();
        final Injector injector = Guice.createInjector(ProgateModule.create(configPath));
        final Vertx vertx = injector.getInstance(Vertx.class);
        final HttpServer server = vertx.createHttpServer();

        final ProgateConfig config = injector.getInstance(ProgateConfig.class);
        setSystemEnvironments(config);

        final ClusterDiscoveryService discovery = injector.getInstance(ClusterDiscoveryService.class);
        final List<Upstream> upstreams = discovery.discover();
        final Set<Outgoing> outgoings = Sets.newHashSet(new GrpcOutgoing());

        final Set<Incoming> inbounds = new InboundLoader().load(config.getInbound(), injector);

        final List<ServiceMetadata> serviceMetadata = loadServices(upstreams);
        // Restful 接口
//        final RestfulIncoming restfulIncoming = new RestfulIncoming(config.getRestful(), hashIdCodec, webClient, config.getOauth2());

        final Router router = Router.router(vertx);
        final HealthyCheck check = new HealthyCheck();
        router.get(check.path).handler(check);
        router.get("/reload").handler(ctx -> {
            inbounds.forEach(inbound -> inbound.reload(serviceMetadata, outgoings));
            ctx.response().end();
        });
        inbounds.forEach(inbound ->
            inbound.apis().forEach(api ->
                api.getMethods().forEach(method -> router.route(method, api.getPath()).handler(context -> inbound.handle(context.request())))
            )
        );

//        restfulIncoming.apis(serviceMetadata).forEach(api -> {
//            api.getMethods().forEach(method -> {
//                router.route(method, api.getPath()).handler(context -> restfulIncoming.handle(api, context.request()));
//            });
//        });

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
                Thread.currentThread().interrupt();
            }
        }));

        final int port = getServerPort(config);
        server.requestHandler(router).listen(port).onSuccess(handler -> logger.info("Server listening on {}", port));

        final EventBus eventBus = vertx.eventBus();

        vertx.setPeriodic(1000, id -> {
            try {
                final List<ServiceMetadata> reloadServiceMetadata = loadServices(upstreams);
                inbounds.forEach(inbound -> inbound.reload(reloadServiceMetadata, outgoings));
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

    private static void setSystemEnvironments(final ProgateConfig config) {
        final Map<String, Object> env = config.getServer().getEnv();
        if (Objects.nonNull(env)) {
            env.forEach((key, value) -> System.setProperty(key, String.valueOf(value)));
        }
    }

    private static List<ServiceMetadata> loadServices(final List<Upstream> upstreams) {
        final ServiceDiscoveryService discovery = new GrpcReflectionServiceDiscovery();
        final List<ServiceMetadata> services = new LinkedList<>();
        upstreams.forEach(upstream -> {
            if (!upstream.isInternal()) {
                services.add(discovery.discover(upstream));
            }
        });
        return services;
    }

    private static int getServerPort(final ProgateConfig config) {
        if (Objects.nonNull(config.getServer()) && Objects.nonNull(config.getServer().getPort())) {
            return config.getServer().getPort();
        }
        return 8080;
    }

}
