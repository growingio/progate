package io.growing.gateway.module;

import java.util.List;

/**
 * @author AI
 */
public interface ModuleScheme {

    String name();

    List<EndpointDefinition> graphqlDefinitions();

    List<EndpointDefinition> restfulDefinitions();

}
