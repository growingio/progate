package io.growing.gateway.config;

import com.google.common.io.Closeables;
import com.google.common.io.Files;
import io.growing.gateway.ConfigFactory;
import io.growing.gateway.exception.ConfigParseException;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;

public class YamlConfigFactoryImpl implements ConfigFactory {
    private final String configPath;

    public YamlConfigFactoryImpl(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public <T> T load(Class<T> clazz) {
        final Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        final Yaml yaml = new Yaml(new Constructor(clazz), representer);
        BufferedReader reader = null;
        try {
            reader = Files.newReader(new File(configPath), StandardCharsets.UTF_8);
            return yaml.load(reader);
        } catch (FileNotFoundException e) {
            throw new ConfigParseException(String.format("Cannot parse config: %s to %s", configPath, clazz.getName()), e);
        } finally {
            Closeables.closeQuietly(reader);
        }
    }

}
