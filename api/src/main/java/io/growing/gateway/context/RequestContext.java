package io.growing.gateway.context;

import java.util.Map;

/**
 * @author AI
 */
public interface RequestContext {

    <T> T getArgument(String name);

    Map<String, Object> getArguments();

}
