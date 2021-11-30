package io.growing.progate.bootstrap.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.growing.gateway.pipeline.Inbound;
import io.growing.progate.bootstrap.config.InboundConfig;

import java.util.Objects;
import java.util.Set;

public class InboundLoader {

    public Set<Inbound> load(final InboundConfig config, final Injector injector) throws ClassNotFoundException {
        Objects.requireNonNull(config, "inbound cannot be empty in config");
        final Config inboundServiceConfig = ConfigFactory.load();
        final ImmutableSet.Builder<Inbound> builder = new ImmutableSet.Builder<>();
        if (Objects.nonNull(config.getGraphql())) {
            final Inbound inbound = createInstance(inboundServiceConfig.getString("inbound.graphql"), injector);
            builder.add(inbound);
        }
        if (Objects.nonNull(config.getRestful())) {
            final Inbound inbound = createInstance(inboundServiceConfig.getString("inbound.restful"), injector);
            builder.add(inbound);
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(final String className, final Injector injector) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        return (T) injector.getInstance(clazz);
    }

}
