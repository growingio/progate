package io.growing.progate.bootstrap.di;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.progate.Resources;
import io.growing.progate.resource.ClassPathResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProgateModuleTest {

    @Test
    void testBuild() {
        final Resources.Resource resource = new ClassPathResource("/gateway.yaml");
        final Injector injector = Guice.createInjector(ProgateModule.create(resource));
        Assertions.assertNotNull(injector.getInstance(GraphqlConfig.class));
        Assertions.assertNotNull(injector.getInstance(HealthService.class));
    }

}
