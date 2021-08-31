package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.graphql.transcode.Transcoder;
import io.growing.gateway.plugin.transcode.EnvironmentArgumentTranscoder;

import java.util.List;
import java.util.Map;

public class DataFetchingEnvironmentContext implements RequestContext, Transcoder {

    private final Map<String, Object> arguments;

    public DataFetchingEnvironmentContext(DataFetchingEnvironment environment, List<String> values, List<String> mappings) {
        final EnvironmentArgumentTranscoder transcoder = new EnvironmentArgumentTranscoder();
        this.arguments = transcoder.transcode(transcode(environment.getArguments(), values, mappings));
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
