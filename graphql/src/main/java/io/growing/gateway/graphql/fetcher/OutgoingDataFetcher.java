package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.graphql.plugin.GraphqlInboundPlugin;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outbound;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<CompletionStage<?>> {
    private final String endpoint;
    private final Upstream upstream;
    private final Outbound outbound;
    private final List<String> values;
    private final List<String> mappings;
    private final boolean isListReturnType;
    private final List<GraphqlInboundPlugin> plugins;

    public OutgoingDataFetcher(String endpoint, Upstream upstream, Outbound outbound,
                               List<GraphqlInboundPlugin> plugins,
                               List<String> values, List<String> mappings, boolean isListReturnType) {
        this.endpoint = endpoint;
        this.upstream = upstream;
        this.outbound = outbound;
        this.values = values;
        this.mappings = mappings;
        this.plugins = plugins;
        this.isListReturnType = isListReturnType;
    }

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        //
        final RequestContext context = new DataFetchingEnvironmentContext(environment, plugins, values, mappings);
        final CompletionStage<?> stage = outbound.handle(upstream, endpoint, context);
        return stage.thenApply(result -> {
            Object value = result;
            for (GraphqlInboundPlugin plugin : plugins) {
                value = plugin.wrapResult(value);
            }
            if (!isListReturnType && value instanceof Collection) {
                final Iterator<?> iterator = ((Collection<?>) value).iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return null;
            }
            return value;
        });
    }

}
