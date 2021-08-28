package io.growing.gateway.graphql.fetcher;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import graphql.schema.DataFetchingEnvironment;
import io.growing.gateway.context.RequestContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DataFetchingEnvironmentContext implements RequestContext {

    private final Map<String, Object> arguments;

    public DataFetchingEnvironmentContext(DataFetchingEnvironment environment, List<String> values, List<String> mappings) {
        final Map<String, Object> parameters = new HashMap<>(environment.getArguments());
        values.forEach(value -> {
            final int index = value.indexOf('=');
            parameters.put(value.substring(0, index), value.substring(index + 1));
        });
        mappings.forEach(mapping -> {
            final int index = mapping.indexOf('=');
            final Object value = parameters.get(mapping.substring(0, index));
            final String to = mapping.substring(index + 1);
            final int dot = to.indexOf('.');
            if (dot > -1) {
                final String name = to.substring(0, dot);
                final String flagAny = "any:";
                final String flagBytes = "bytes";
                if (to.indexOf(flagAny, dot) > -1) {
                    final Map<String, Object> object = (Map<String, Object>) value;
                    object.put("@type", to.substring(dot + 1).replace(flagAny, ""));
                    parameters.put(name, value);
                } else if (to.indexOf(flagBytes, dot) > -1) {
                    String bytesString;
                    if (value instanceof Map) {
                        bytesString = new Gson().toJson(value);
                    } else {
                        bytesString = String.valueOf(value);
                    }
                    parameters.put(name, ByteString.copyFromUtf8(bytesString));
                } else {
                    parameters.put(name, new Object[]{value});
                }
            } else {
                parameters.put(to, value);
            }
        });
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        parameters.forEach((key, value) -> {
            if (Objects.nonNull(value)) {
                builder.put(key, value);
            }
        });
        this.arguments = builder.build();
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
