package io.growing.progate.bootstrap;

import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.discovery.ServiceDiscoveryService;
import io.growing.gateway.grpc.GrpcOutgoing;
import io.growing.gateway.grpc.discovery.GrpcReflectionServiceDiscovery;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Inbound;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.progate.bootstrap.config.ConfigEntry;
import io.growing.progate.bootstrap.config.ProgateConfig;
import io.growing.gateway.context.GuiceRuntimeContext;
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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

/**
 * @author AI
 */
public class ProgateBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(ProgateBootstrap.class);

    public static void main(final String[] args) throws Exception {
        final String configPath = ProgateModule.getApplicationConfigFile(args);
        final Injector injector = Guice.createInjector(ProgateModule.create(configPath));
        final Vertx vertx = injector.getInstance(Vertx.class);
        final HttpServer server = vertx.createHttpServer();

        final RuntimeContext runtimeContext = GuiceRuntimeContext.from(configPath, injector);

        final ProgateConfig config = injector.getInstance(ProgateConfig.class);
        setSystemEnvironments(config);

        final ClusterDiscoveryService discovery = injector.getInstance(ClusterDiscoveryService.class);
        final List<Upstream> upstreams = discovery.discover();
        final Set<Outgoing> outgoings = Sets.newHashSet(new GrpcOutgoing());

        final Set<Inbound> inbounds = new InboundLoader().load(config.getInbound(), injector);

        final Router router = Router.router(vertx);
        final HealthyCheck check = new HealthyCheck();
        router.get(check.path).handler(check);
        router.get("/reload").handler(ctx -> {
            inbounds.forEach(inbound -> inbound.reload(loadServices(upstreams), outgoings, runtimeContext));
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
                Thread.currentThread().interrupt();
            }
        }));

        final int port = getServerPort(config);
        server.requestHandler(router).listen(port).onSuccess(handler -> logger.info("Server listening on {}", port));

        final EventBus eventBus = vertx.eventBus();

        vertx.setPeriodic(1000, id -> {
            try {
                final List<ServiceMetadata> services = loadServices(upstreams);
                inbounds.forEach(inbound -> inbound.reload(services, outgoings, runtimeContext));
                inbounds.forEach(inbound ->
                    inbound.apis(services).forEach(api ->
                        api.getMethods().forEach(method -> router.route(method, api.getPath()).handler(context -> inbound.handle(context.request())))
                    )
                );
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
        final List<ConfigEntry> env = config.getServer().getEnv();
        if (Objects.nonNull(env)) {
            env.forEach(entry -> System.setProperty(entry.getName(), String.valueOf(entry.getValue())));
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
