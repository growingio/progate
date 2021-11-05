package io.growing.progate.bootstrap.di;

import com.google.inject.AbstractModule;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.config.YamlConfigFactoryImpl;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.grpc.ctrl.GrpcHealthService;
import io.growing.gateway.internal.discovery.ConfigClusterDiscoveryService;
import io.growing.progate.bootstrap.config.ProgateConfig;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import org.apache.commons.lang3.SystemUtils;

import java.nio.file.Paths;
import java.util.Objects;

public class ProgateModule extends AbstractModule {

    private final String configPath;

    private ProgateModule(String configPath) {
        this.configPath = configPath;
    }

    public static ProgateModule create(final String configPath) {
        return new ProgateModule(configPath);
    }

    public static String getApplicationConfigFile() {
        final String property = System.getProperty("config.file");
        if (Objects.nonNull(property)) {
            return property;
        }
        return Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
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
        if (Objects.nonNull(progateConfig.getInbound()) && Objects.nonNull(progateConfig.getInbound().getGraphql())) {
            bind(GraphqlConfig.class).toInstance(progateConfig.getInbound().getGraphql());
        }
        bind(ClusterDiscoveryService.class).to(ConfigClusterDiscoveryService.class);
        bind(HealthService.class).to(GrpcHealthService.class);
    }

}
