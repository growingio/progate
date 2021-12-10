package io.growing.progate.bootstrap.loader;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.pipeline.Inbound;
import io.growing.progate.bootstrap.config.InboundConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class InboundLoaderTest {

    @Test
    void testLoad() throws ClassNotFoundException {
        final InboundConfig config = new InboundConfig();
        config.setGraphql(new GraphqlConfig());
        config.setRestful(null);
        final InboundLoader loader = new InboundLoader();
        final Injector injector = Guice.createInjector();
        final Set<Inbound> inbounds = loader.load(config, injector);
        Assertions.assertNotNull(inbounds);
        Assertions.assertEquals(1, inbounds.size());
        Assertions.assertTrue(inbounds.iterator().next() instanceof MockInbound);
    }

}
