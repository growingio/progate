package io.growing.gateway.graphql.idl;

import graphql.relay.Relay;
import graphql.schema.DataFetcher;
import graphql.schema.FieldCoordinates;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLFieldsContainer;
import graphql.schema.PropertyDataFetcher;
import graphql.schema.idl.SchemaDirectiveWiring;
import graphql.schema.idl.SchemaDirectiveWiringEnvironment;

public class GlobalIdSchemaDirectiveWiring implements SchemaDirectiveWiring {

    public static final String NAME = "globalId";
    private final Relay relay = new Relay();

    @Override
    public GraphQLFieldDefinition onField(SchemaDirectiveWiringEnvironment<GraphQLFieldDefinition> environment) {
        final GraphQLFieldDefinition field = environment.getElement();
        final PropertyDataFetcher<?> fetcher = PropertyDataFetcher.fetching(field.getName());
        final GraphQLFieldsContainer parentType = environment.getFieldsContainer();
        environment.getCodeRegistry().dataFetcher(FieldCoordinates.coordinates(parentType, field), (DataFetcher<Object>) env -> {
            final Object value = fetcher.get(env);
            return relay.toGlobalId(parentType.getName(), String.valueOf(value));
        });
        return field;
    }

}
