package io.growing.gateway.meta;

import io.growing.gateway.module.EndpointDefinition;

import java.util.List;

public interface ServiceMetadata {

    Upstream upstream();

    List<EndpointDefinition> graphqlDefinitions();

    List<EndpointDefinition> restfulDefinitions();

}
