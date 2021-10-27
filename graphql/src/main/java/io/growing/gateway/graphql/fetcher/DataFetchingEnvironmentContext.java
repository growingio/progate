package io.growing.gateway.graphql.fetcher;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.graphql.transcode.Transcoder;
import io.growing.gateway.plugin.transcode.EnvironmentArgumentTranscoder;

import java.util.List;
import java.util.Map;

public class DataFetchingEnvironmentContext implements RequestContext, Transcoder {

    private final Map<String, Object> arguments;
    private final DataFetchingEnvironment environment;

    public DataFetchingEnvironmentContext(DataFetchingEnvironment environment, List<String> values, List<String> mappings) {
        this.environment = environment;
        final EnvironmentArgumentTranscoder transcoder = new EnvironmentArgumentTranscoder(environment);
        final GraphQLContext context = environment.getGraphQlContext();
        this.arguments = transcoder.transcode(transcode(context, environment.getArguments(), values, mappings));
    }

    @Override
    public String id() {
        return environment.getExecutionId().toString();
    }

    @Override
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }

    @Override
    public Map<String, Object> getArguments() {
        return arguments;
    }

}
