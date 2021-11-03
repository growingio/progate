package io.growing.gateway.app;

import com.google.common.collect.Sets;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.config.YamlConfigFactoryImpl;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.discovery.ServiceDiscoveryService;
import io.growing.gateway.graphql.GraphqlIncoming;
import io.growing.gateway.grpc.GrpcOutgoing;
import io.growing.gateway.grpc.ctrl.GrpcHealthService;
import io.growing.gateway.grpc.discovery.GrpcReflectionServiceDiscovery;
import io.growing.gateway.internal.discovery.ConfigClusterDiscoveryService;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.iam.UserService;
import io.growing.gateway.plugin.lang.HashIdCodec;
import io.growing.gateway.restful.RestfulIncoming;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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

        final GlobalConfig config = new YamlConfigFactoryImpl(configPath).load(GlobalConfig.class);

        if (Objects.nonNull(config.getHashids())) {
            System.setProperty("hashids.salt", config.getHashids().getSalt());
        }
        final HealthService healthService = new GrpcHealthService(vertx);
        final WebClient webClient = WebClient.create(vertx);
        final ConfigFactory configFactory = new YamlConfigFactoryImpl(configPath);
        final ClusterDiscoveryService discovery = new ConfigClusterDiscoveryService(configPath, healthService, configFactory);
        final List<Upstream> upstreams = discovery.discover();

        upstreams.forEach(upstream -> {
            if (upstream.isInternal()) {
                UserService.upstream(upstream);
            }
        });
        final List<ServiceMetadata> serviceMetadata = loadServices(upstreams);
        // Grpc 接口
        final Set<Outgoing> outgoings = Sets.newHashSet(new GrpcOutgoing());
        final GraphqlIncoming graphqlIncoming = new GraphqlIncoming(config.getGraphql(), configFactory);
        final HashIdCodec hashIdCodec = new HashIdCodec(config.getHashids().getSalt(), config.getHashids().getLength());
        // Restful 接口
        final RestfulIncoming restfulIncoming = new RestfulIncoming(config.getRestful(), hashIdCodec, WebClient.create(vertx), config.getOauth2());
        // 先加载
        router.get("/reload").handler(ctx -> {
            graphqlIncoming.reload(serviceMetadata, outgoings);
            restfulIncoming.reload(serviceMetadata, outgoings);
            ctx.response().end();
        });
        // 设置路由
        graphqlIncoming.apis().forEach(api -> {
            api.getMethods().forEach(method -> {
                router.route(method, api.getPath()).handler(context -> graphqlIncoming.handle(context.request()));
            });
        });
        restfulIncoming.apis(serviceMetadata).forEach(api -> {
            api.getMethods().forEach(method -> {
                router.route(method, api.getPath()).handler(context -> restfulIncoming.handle(api, context.request()));
            });
        });

        final HealthyCheck check = new HealthyCheck();

        router.get(check.path).handler(check);

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
                graphqlIncoming.reload(reloadServiceMetadata, outgoings);
                restfulIncoming.reload(reloadServiceMetadata, outgoings);
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
        final String property = System.getProperty("config.file");
        if (Objects.nonNull(property)) {
            return property;
        }
        return Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
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

    private static int getServerPort(final GlobalConfig config) {
        if (Objects.nonNull(config.getServer()) && Objects.nonNull(config.getServer().getPort())) {
            return config.getServer().getPort();
        }
        return 8080;
    }

}
