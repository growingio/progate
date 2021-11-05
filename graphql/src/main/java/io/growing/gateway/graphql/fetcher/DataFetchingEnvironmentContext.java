package io.growing.gateway.graphql.fetcher;

import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.graphql.plugin.GraphqlInboundPlugin;
import io.growing.gateway.graphql.transcode.Transcoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataFetchingEnvironmentContext implements RequestContext, Transcoder {

    private final Map<String, Object> arguments;
    private final DataFetchingEnvironment environment;

    public DataFetchingEnvironmentContext(DataFetchingEnvironment environment,
                                          List<GraphqlInboundPlugin> plugins,
                                          List<String> values, List<String> mappings) {
        this.environment = environment;
        this.arguments = new HashMap<>(environment.getArguments());
        plugins.forEach(plugin -> plugin.transcodeArguments(environment, arguments));
        transcode(arguments, values, mappings);
    }

    @Override
    public String id() {
        return environment.getExecutionId().toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }

    @Override
    public Map<String, Object> getArguments() {
        return arguments;
    }

}
