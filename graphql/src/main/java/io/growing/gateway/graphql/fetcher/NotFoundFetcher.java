package io.growing.gateway.graphql.fetcher;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.graphql.exception.FetcherNotFoundException;

public class NotFoundFetcher implements DataFetcher<DataFetcherResult> {
    @Override
    public DataFetcherResult get(DataFetchingEnvironment environment) throws Exception {
        final String name = environment.getField().getName();
        return DataFetcherResult.newResult().data(null).error(new FetcherNotFoundException("Cannot found fetcher: " + name, name)).build();
    }
}
