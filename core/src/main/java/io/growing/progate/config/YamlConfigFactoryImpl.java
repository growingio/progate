package io.growing.progate.config;

import io.growing.gateway.config.ConfigFactory;
import io.growing.progate.Resources;
import io.growing.progate.exception.ConfigParseException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;

public class YamlConfigFactoryImpl implements ConfigFactory {
    private final Resources.Resource resource;

    public YamlConfigFactoryImpl(Resources.Resource resource) {
        this.resource = resource;
    }

    @Override
    public <T> T load(Class<T> clazz) {
        final Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        final Yaml yaml = new Yaml(new Constructor(clazz), representer);
        try {
            return yaml.load(resource.utf8String());
        } catch (IOException e) {
            throw new ConfigParseException(String.format("Cannot parse config: %s to %s", resource.uri(), clazz.getName()), e);
        }
    }

}
