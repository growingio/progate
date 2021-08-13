package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;

import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public class OutgoingDataFetcher implements DataFetcher<CompletionStage<? extends Object>> {
    private final String endpoint;
    private final Upstream upstream;
    private final OutgoingHandler outgoing;

    public OutgoingDataFetcher(String endpoint, Upstream upstream, OutgoingHandler outgoing) {
        this.endpoint = endpoint;
        this.upstream = upstream;
        this.outgoing = outgoing;
    }

    @Override
    public CompletionStage<? extends Object> get(DataFetchingEnvironment environment) throws Exception {
        return outgoing.handle(upstream, endpoint, null);
    }

}
