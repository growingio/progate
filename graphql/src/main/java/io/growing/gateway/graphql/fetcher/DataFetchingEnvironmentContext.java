package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;

import java.util.Map;

public class DataFetchingEnvironmentContext implements RequestContext {

    private final DataFetchingEnvironment environment;

    public DataFetchingEnvironmentContext(DataFetchingEnvironment environment) {
        this.environment = environment;
    }

    @Override
    public <T> T getArgument(String name) {
        return environment.getArgument(name);
    }

    @Override
    public Map<String, Object> getArguments() {
        return environment.getArguments();
    }
}
