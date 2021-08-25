package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;

import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<CompletionStage<?>> {
    private final String endpoint;
    private final Upstream upstream;
    private final Outgoing outgoing;

    public OutgoingDataFetcher(String endpoint, Upstream upstream, Outgoing outgoing) {
        this.endpoint = endpoint;
        this.upstream = upstream;
        this.outgoing = outgoing;
    }

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        //
        final RequestContext context = new DataFetchingEnvironmentContext(environment);
        return outgoing.handle(upstream, endpoint, context);
        //
    }

}
