package io.growing.gateway.context;

/**
 * @author AI
 */
public interface RequestContext {

    public <T> T getArgument(String name);

}
