package io.growing.gateway.progate.plugin;

import graphql.schema.DataFetcher;
import io.growing.gateway.graphql.plugin.GraphqlInboundPlugin;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;

class GraphqlInboundPluginTest {

    @Test
    void testDefaultMethods() {
        final GraphqlInboundPlugin plugin = (config, context) -> {
        };
        Assertions.assertTrue(plugin.resolvers().isEmpty());
        Assertions.assertTrue(plugin.scalars().isEmpty());
        Assertions.assertTrue(plugin.arguments(null).isEmpty());
        final Object result = new Object();
        Assertions.assertEquals(result, plugin.wrapResult(result));
        final DataFetcher<CompletionStage<?>> fetcher = environment -> null;
        Assertions.assertEquals(fetcher, plugin.fetcherChain(null, fetcher));
        Assertions.assertDoesNotThrow(() -> plugin.transcodeArguments(null, null));
    }

}
