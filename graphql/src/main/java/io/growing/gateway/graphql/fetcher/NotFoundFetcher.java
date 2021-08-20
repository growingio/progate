package io.growing.gateway.graphql.fetcher;

import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.graphql.exception.FetcherNotFoundException;

public class NotFoundFetcher implements DataFetcher<DataFetcherResult<?>> {

    @Override
    public DataFetcherResult<?> get(DataFetchingEnvironment environment) throws Exception {
        throw new FetcherNotFoundException();
    }

}
