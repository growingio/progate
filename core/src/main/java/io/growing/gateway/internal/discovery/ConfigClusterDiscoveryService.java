package io.growing.gateway.internal.discovery;

import io.growing.gateway.ConfigFactory;
import io.growing.gateway.config.UpstreamConfig;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.discovery.ClusterDiscoveryService;
import io.growing.gateway.meta.Upstream;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public class ConfigClusterDiscoveryService implements ClusterDiscoveryService {
    private final String configPath;
    private final HealthService healthService;
    private final ConfigFactory configFactory;

    public ConfigClusterDiscoveryService(String configPath, HealthService healthService, ConfigFactory configFactory) {
        this.configPath = configPath;
        this.healthService = healthService;
        this.configFactory = configFactory;
    }

    @Override
    public List<Upstream> discover() {
        final AppConfig config = configFactory.load(AppConfig.class);
        return config.upstreams.stream().map(u -> u.toUpstream(healthService)).collect(Collectors.toList());
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