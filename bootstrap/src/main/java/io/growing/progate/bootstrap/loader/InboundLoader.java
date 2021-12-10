package io.growing.progate.bootstrap.loader;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.growing.gateway.pipeline.Inbound;
import io.growing.progate.bootstrap.config.InboundConfig;
import io.growing.progate.bootstrap.utils.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class InboundLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(InboundLoader.class);

    public Set<Inbound> load(final InboundConfig config, final Injector injector) throws ClassNotFoundException {
        Objects.requireNonNull(config, "inbound cannot be empty in config");
        final Config inboundServiceConfig = ConfigFactory.load();
        final ImmutableSet.Builder<Inbound> builder = new ImmutableSet.Builder<>();
        final List<PropertyDescriptor> componentProperties = ConfigUtils.getInboundComponentProperties(config);
        for (PropertyDescriptor componentProperty : componentProperties) {
            final String inboundPath = "inbound." + componentProperty.getName();
            final String inboundClassName = inboundServiceConfig.getString(inboundPath);
            final Inbound inbound = createInstance(inboundClassName, injector);
            builder.add(inbound);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Found {}: {}", inboundPath, inboundClassName);
            }
        }
        return builder.build();
    }

    @SuppressWarnings("unchecked")
    private <T> T createInstance(final String className, final Injector injector) throws ClassNotFoundException {
        final Class<?> clazz = Class.forName(className);
        return (T) injector.getInstance(clazz);
    }


}
