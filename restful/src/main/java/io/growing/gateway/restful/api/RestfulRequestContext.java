package io.growing.gateway.restful.api;

import io.growing.gateway.context.RequestContext;

import java.util.Map;

/**
 * @author zhuhongbin
 */
public class RestfulRequestContext implements RequestContext {

    private final Map<String, Object> arguments;

    public RestfulRequestContext(Map<String, Object> arguments) {
        this.arguments = arguments;
    }

    @Override
    public String id() {
        return (String) arguments.get("id");
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
