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
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.graphql.fetcher.NotFoundFetcher;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.module.ModuleScheme;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class GraphqlBuilder {

    private List<Upstream> upstreams;
    private Set<OutgoingHandler> outgoings;
    private final GraphqlSchemaParser parser = new GraphqlSchemaParser();

    public static GraphqlBuilder newBuilder() {
        return new GraphqlBuilder();
    }

    public GraphqlBuilder upstreams(final List<Upstream> upstreams) {
        this.upstreams = upstreams;
        return this;
    }

    public GraphqlBuilder outgoings(final Set<OutgoingHandler> outgoings) {
        this.outgoings = outgoings;
        return this;
    }

    public GraphQL build() {
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        final Map<String, OutgoingHandler> handlers = new HashMap<>(outgoings.size());
        outgoings.forEach(handler -> handlers.put(handler.protocol(), handler));
        final List<ModuleScheme> schemes = new LinkedList<>();
        upstreams.forEach(upstream -> {
            final OutgoingHandler handler = handlers.get(upstream.getProtocol());
            final ModuleScheme module = handler.load(upstream);
            bindDataFetcher(runtimeWiringBuilder, module, upstream, handlers);
            schemes.add(module);
        });

        final TypeDefinitionRegistry registry = parser.parse(schemes);
        final SchemaGenerator generator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void bindDataFetcher(final RuntimeWiring.Builder register, final ModuleScheme module,
                                 final Upstream upstream, final Map<String, OutgoingHandler> handlers) {
        final TypeDefinitionRegistry registry = parser.parse(module);
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
                        final OutgoingHandler handler = handlers.get(endpointDirective.getName());
                        final DataFetcher<CompletionStage<?>> fetcher = new OutgoingDataFetcher(endpoint, upstream, handler);
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
