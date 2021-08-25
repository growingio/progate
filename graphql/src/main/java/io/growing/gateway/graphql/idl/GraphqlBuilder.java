package io.growing.gateway.graphql.idl;

import graphql.GraphQL;
import graphql.language.Directive;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.StringValue;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.fetcher.NotFoundFetcher;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GraphqlBuilder {

    private Set<Outgoing> outgoings;
    private List<ServiceMetadata> services;
    private final GraphqlSchemaParser parser = new GraphqlSchemaParser();

    public static GraphqlBuilder newBuilder() {
        return new GraphqlBuilder();
    }

    public GraphqlBuilder services(final List<ServiceMetadata> services) {
        this.services = services;
        return this;
    }

    public GraphqlBuilder outgoings(final Set<Outgoing> outgoings) {
        this.outgoings = outgoings;
        return this;
    }

    public GraphQL build() {
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        final Map<String, Outgoing> handlers = new HashMap<>(outgoings.size());
        outgoings.forEach(handler -> handlers.put(handler.protocol(), handler));
        services.forEach(service -> {
            bindDataFetcher(runtimeWiringBuilder, service, handlers);
        });

        final TypeDefinitionRegistry registry = parser.parse(services);
        final SchemaGenerator generator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void bindDataFetcher(final RuntimeWiring.Builder register, final ServiceMetadata service, final Map<String, Outgoing> handlers) {
        final TypeDefinitionRegistry registry = parser.parse(service);
        final Set<String> protocols = handlers.keySet();
        final Consumer<String> bind = (final String type) -> {
            final ObjectTypeDefinition typeDef = (ObjectTypeDefinition) registry.types().get(type);
            if (Objects.isNull(typeDef)) {
                return;
            }
            final List<FieldDefinition> fields = typeDef.getFieldDefinitions();
            fields.forEach(field -> {
                try (final Stream<Directive> stream = field.getDirectives().stream()) {
                    final Optional<Directive> endpointDirectiveOpt = stream.filter(directive -> protocols.contains(directive.getName())).findAny();
                    if (endpointDirectiveOpt.isPresent()) {
                        final Directive endpointDirective = endpointDirectiveOpt.get();
                        final String endpoint = ((StringValue) endpointDirective.getArgument("endpoint").getValue()).getValue();
                        final Outgoing handler = handlers.get(endpointDirective.getName());
                        final DataFetcher<CompletionStage<?>> fetcher = new OutgoingDataFetcher(endpoint, service.upstream(), handler);
                        register.type(type, builder -> builder.dataFetcher(field.getName(), fetcher));
                    } else {
                        register.type(type, builder -> builder.dataFetcher(field.getName(), new NotFoundFetcher()));
                    }
                }
            });
        };
        bind.accept("Query");
        bind.accept("Mutation");
    }

}
