package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.graphql.exception.FetcherNotFoundException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class NotFoundFetcher implements DataFetcher<CompletionStage<?>> {

    @Override
    public CompletionStage<?> get(DataFetchingEnvironment environment) throws Exception {
        return CompletableFuture.failedFuture(new FetcherNotFoundException());
    }

}
