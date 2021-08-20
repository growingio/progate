package io.growing.gateway.internal;

import com.google.common.io.Files;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.config.UpstreamConfig;
import io.growing.gateway.discovery.UpstreamDiscovery;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public class ConfigUpstreamDiscovery implements UpstreamDiscovery {
    private final String configPath;

    public ConfigUpstreamDiscovery(String configPath) {
        this.configPath = configPath;
    }

    @Override
    public List<Upstream> discover() {
        final Yaml yaml = new Yaml();
        try {
            BufferedReader reader = Files.newReader(new File(configPath), StandardCharsets.UTF_8);
            final AppConfig config = yaml.loadAs(reader, AppConfig.class);
            return config.upstreams.stream().map(UpstreamConfig::toUpstream).collect(Collectors.toList());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e.getLocalizedMessage(), e);
        }
    }

    public static class AppConfig {
        private List<UpstreamConfig> upstreams;

        public List<UpstreamConfig> getUpstreams() {
            return upstreams;
        }

        public void setUpstreams(List<UpstreamConfig> upstreams) {
            this.upstreams = upstreams;
        }
    }

}
