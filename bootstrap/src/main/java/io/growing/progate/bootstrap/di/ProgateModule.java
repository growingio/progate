package io.growing.progate.bootstrap.di;

import com.google.inject.AbstractModule;
import io.growing.gateway.config.ConfigFactory;
import io.growing.progate.config.YamlConfigFactoryImpl;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.grpc.ctrl.GrpcHealthService;
import io.growing.progate.internal.discovery.ConfigClusterDiscoveryService;
import io.growing.progate.restful.config.RestfulConfig;
import io.growing.progate.bootstrap.config.ProgateConfig;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.Objects;

public class ProgateModule extends AbstractModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProgateModule.class);

    private final String configPath;

    private ProgateModule(String configPath) {
        this.configPath = configPath;
    }

    public static ProgateModule create(final String configPath) {
        return new ProgateModule(configPath);
    }

    public static String getApplicationConfigFile(final String[] args) {
        final String key = "config.path";
        final String argSetter = key + "=";
        for (String arg : args) {
            if (arg.startsWith(argSetter)) {
                final String path = arg.replace(argSetter, "");
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("Read config file from jvm args, {}", path);
                }
                return path;
            }
        }
        final String property = System.getProperty(key);
        if (Objects.nonNull(property)) {
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Read config file from jvm environment, {}", property);
            }
            return property;
        }
        final String path = Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Read config file from user.dir, {}", path);
        }
        return path;
    }

    @Override
    protected void configure() {
        final Vertx vertx = Vertx.vertx();
        final EventBus eventBus = vertx.eventBus();
        bind(Vertx.class).toInstance(vertx);
        bind(EventBus.class).toInstance(eventBus);
        final ConfigFactory configFactory = new YamlConfigFactoryImpl(configPath);
        bind(ConfigFactory.class).toInstance(configFactory);
        final ProgateConfig progateConfig = configFactory.load(ProgateConfig.class);
        bind(ProgateConfig.class).toInstance(progateConfig);
        if (Objects.nonNull(progateConfig.getInbound())) {
            if (Objects.nonNull(progateConfig.getInbound().getGraphql())) {
                bind(GraphqlConfig.class).toInstance(progateConfig.getInbound().getGraphql());
            }
            if (Objects.nonNull(progateConfig.getInbound().getRestful())) {
                bind(RestfulConfig.class).toInstance(progateConfig.getInbound().getRestful());
            }
        }
        bind(ClusterDiscoveryService.class).to(ConfigClusterDiscoveryService.class);
        bind(HealthService.class).to(GrpcHealthService.class);
    }

}
