package io.growing.gateway.graphql.plugin;

import graphql.schema.TypeResolver;

public interface SchemaObjectResolver extends TypeResolver {

    String name();

}
