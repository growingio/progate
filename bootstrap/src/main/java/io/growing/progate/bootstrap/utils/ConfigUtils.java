package io.growing.progate.bootstrap.utils;

import com.google.common.collect.ImmutableList;
import io.growing.progate.bootstrap.config.Component;
import io.growing.progate.bootstrap.config.InboundConfig;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public final class ConfigUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigUtils.class);

    private ConfigUtils() {
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
        final String path = getDefaultConfigPath();
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Read config file from user.dir, {}", path);
        }
        return path;
    }

    public static String getDefaultConfigPath() {
        return Paths.get(SystemUtils.getUserDir().getAbsolutePath(), "conf", "gateway.yaml").toAbsolutePath().toString();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getInboundComponent(final InboundConfig config, final PropertyDescriptor property) {
        try {
            return (T) property.getReadMethod().invoke(config);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public static List<PropertyDescriptor> getInboundComponentProperties(final InboundConfig config) {
        final ImmutableList.Builder<PropertyDescriptor> builder = new ImmutableList.Builder<>();
        try {
            final BeanInfo beanInfo = Introspector.getBeanInfo(config.getClass());
            for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
                final Method readMethod = descriptor.getReadMethod();
                if (Objects.isNull(readMethod)) {
                    continue;
                }
                final Component component = readMethod.getAnnotation(Component.class);
                if (Objects.nonNull(component)) {
                    final Object value = readMethod.invoke(config);
                    if (Objects.nonNull(value)) {
                        builder.add(descriptor);
                    }
                }
            }
        } catch (IntrospectionException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return builder.build();
    }

}
