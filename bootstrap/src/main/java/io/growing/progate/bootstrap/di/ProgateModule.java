package io.growing.progate.bootstrap.di;

import com.google.inject.AbstractModule;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.grpc.ctrl.GrpcHealthService;
import io.growing.progate.Resources;
import io.growing.progate.bootstrap.config.ProgateConfig;
import io.growing.progate.bootstrap.utils.ConfigUtils;
import io.growing.progate.config.YamlConfigFactoryImpl;
import io.growing.progate.internal.discovery.ConfigClusterDiscoveryService;
import io.growing.progate.resource.URLResource;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Objects;

public class ProgateModule extends AbstractModule {

    private final Resources.Resource configResource;

    private ProgateModule(Resources.Resource configResource) {
        this.configResource = configResource;
    }

    public static ProgateModule create(final String configPath) {
        return new ProgateModule(new URLResource("file://" + configPath));
    }

    public static ProgateModule create(final Resources.Resource configResource) {
        return new ProgateModule(configResource);
    }

    @Override
    protected void configure() {
        final Vertx vertx = Vertx.vertx();
        final EventBus eventBus = vertx.eventBus();
        bind(Vertx.class).toInstance(vertx);
        bind(EventBus.class).toInstance(eventBus);
        final ConfigFactory configFactory = new YamlConfigFactoryImpl(configResource);
        bind(ConfigFactory.class).toInstance(configFactory);
        final ProgateConfig progateConfig = configFactory.load(ProgateConfig.class);
        bind(ProgateConfig.class).toInstance(progateConfig);
        if (Objects.nonNull(progateConfig.getInbound())) {
            final List<PropertyDescriptor> componentProperties = ConfigUtils.getInboundComponentProperties(progateConfig.getInbound());
            componentProperties.forEach(property ->
                bind(property.getPropertyType()).toInstance(ConfigUtils.getInboundComponent(progateConfig.getInbound(), property)));
        }
        bind(ClusterDiscoveryService.class).to(ConfigClusterDiscoveryService.class);
        bind(HealthService.class).to(GrpcHealthService.class);
    }

}
