package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.transcode.ResultWrapper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

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
            final Object value = wrap(result);
            if (!isListReturnType && value instanceof Collection) {
                final Iterator<?> iterator = ((Collection<?>) value).iterator();
                if (iterator.hasNext()) {
                    return iterator.next();
                }
                return null;
            }
            return value;
        });
        //
    }

    @SuppressWarnings("unchecked")
    private Object wrap(final Object value) {
        if (value instanceof Collection) {
            return ((Collection) value).stream().map(v -> new ResultWrapper((Map<String, Object>) v))
                .collect(Collectors.toList());
        }
        return new ResultWrapper((Map<String, Object>) value);
    }

}
