package io.growing.progate.discovery;

import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.meta.Upstream;
import io.growing.progate.config.YamlConfigFactoryImpl;
import io.growing.progate.internal.discovery.ConfigClusterDiscoveryService;
import io.growing.progate.resource.ClassPathResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;

class ConfigClusterDiscoveryServiceTest {
    @Test
    void test() {
        final HealthService healthService = Mockito.mock(HealthService.class);
        final ConfigFactory cf = new YamlConfigFactoryImpl(new ClassPathResource("/upstreams.yaml"));
        final ConfigClusterDiscoveryService cds = new ConfigClusterDiscoveryService(healthService, cf);
        final List<Upstream> upstreams = cds.discover();
        Assertions.assertFalse(upstreams.isEmpty());
        final Optional<Upstream> upstreamOpt = upstreams.stream().findFirst();
        Assertions.assertTrue(upstreamOpt.isPresent());
        Assertions.assertEquals("metadata", upstreamOpt.get().name());
    }
}
