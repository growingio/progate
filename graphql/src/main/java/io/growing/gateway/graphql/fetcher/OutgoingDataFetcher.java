package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<CompletionStage<?>> {
    private final String endpoint;
    private final Upstream upstream;
    private final Outgoing outgoing;
    private final List<String> values;
    private final List<String> mappings;
    private final boolean isListReturnType;

    public OutgoingDataFetcher(String endpoint, Upstream upstream, Outgoing outgoing,
                               List<String> values, List<String> mappings, boolean isListReturnType) {
        this.endpoint = endpoint;
        this.upstream = upstream;
        this.outgoing = outgoing;
        this.values = values;
        this.mappings = mappings;
        this.isListReturnType = isListReturnType;
    }

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        //
        final RequestContext context = new DataFetchingEnvironmentContext(environment, values, mappings);
        final CompletionStage<?> stage = outgoing.handle(upstream, endpoint, context);
        return stage.thenApply(result -> {
            if (!isListReturnType && result instanceof Collection) {
                return ((Collection<?>) result).iterator().next();
            }
            return result;
        });
        //
    }

}
