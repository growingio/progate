package io.growing.gateway.graphql.idl;

import com.google.common.collect.Sets;
import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.language.Argument;
import graphql.language.ArrayValue;
import graphql.language.Directive;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.Node;
import graphql.language.ObjectTypeDefinition;
import graphql.language.StringValue;
import graphql.language.Type;
import graphql.scalars.ExtendedScalars;
import graphql.scalars.java.JavaPrimitives;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.FetchSchemaDirectiveWiring;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.TypeRuntimeWiring;
import io.growing.gateway.graphql.fetcher.AccessLogFetcher;
import io.growing.gateway.graphql.fetcher.NotFoundFetcher;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.graphql.plugin.GraphqlInboundPlugin;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outbound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class GraphqlBuilder {

    private Set<String> schemas;
    private Set<Outbound> outbounds;
    private List<ServiceMetadata> services;
    private List<GraphqlInboundPlugin> plugins;
    private DataFetcherExceptionHandler exceptionHandler;
    private final Set<GraphQLScalarType> scalars = Sets.newHashSet(ExtendedScalars.Json, ExtendedScalars.Object, JavaPrimitives.GraphQLLong);

    public static GraphqlBuilder newBuilder() {
        return new GraphqlBuilder();
    }

    public GraphqlBuilder schemas(final Set<String> schemas) {
        this.schemas = new HashSet<>();
        this.schemas.addAll(schemas);
        return this;
    }

    public GraphqlBuilder services(final List<ServiceMetadata> services) {
        this.services = services;
        return this;
    }

    public GraphqlBuilder outgoings(final Set<Outbound> outbounds) {
        this.outbounds = outbounds;
        return this;
    }

    public GraphqlBuilder exceptionHandler(final DataFetcherExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public GraphqlBuilder plugins(List<GraphqlInboundPlugin> plugins) {
        this.plugins = plugins;
        return this;
    }

    public GraphQL build() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(schemas);
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        final Map<String, Outbound> handlers = new HashMap<>(outbounds.size());
        outbounds.forEach(handler -> handlers.put(handler.protocol(), handler));
        services.forEach(service -> bindDataFetcher(parser, runtimeWiringBuilder, service, handlers));
        runtimeWiringBuilder.directive(GlobalIdSchemaDirectiveWiring.NAME, new GlobalIdSchemaDirectiveWiring());
        runtimeWiringBuilder.directive(FetchSchemaDirectiveWiring.FETCH,new FetchSchemaDirectiveWiring());
        scalars.forEach(runtimeWiringBuilder::scalar);
        plugins.forEach(plugin -> {
            plugin.resolvers().forEach(resolver -> {
                final TypeRuntimeWiring.Builder type = TypeRuntimeWiring.newTypeWiring(resolver.name()).typeResolver(resolver);
                runtimeWiringBuilder.type(type);
            });
            plugin.scalars().forEach(runtimeWiringBuilder::scalar);
        });
        final TypeDefinitionRegistry registry = parser.parse(services);
        final SchemaGenerator generator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        return GraphQL.newGraphQL(graphQLSchema)
            .defaultDataFetcherExceptionHandler(exceptionHandler)
            .queryExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler))
            .mutationExecutionStrategy(new AsyncExecutionStrategy(exceptionHandler)).build();
    }

    private void bindDataFetcher(final GraphqlSchemaParser parser, final RuntimeWiring.Builder register,
                                 final ServiceMetadata service, final Map<String, Outbound> handlers) {
        final TypeDefinitionRegistry registry = parser.parse(service);
        final Set<String> protocols = handlers.keySet();
        final Consumer<String> bind = (final String type) -> {
            final ObjectTypeDefinition typeDef = (ObjectTypeDefinition) registry.types().get(type);
            if (Objects.isNull(typeDef)) {
                return;
            }
            final List<FieldDefinition> fields = typeDef.getFieldDefinitions();
            fields.forEach(field -> {
                final List<Directive> directives = field.getDirectives();
                final Optional<Directive> endpointDirectiveOpt = directives.stream()
                    .filter(directive -> protocols.contains(directive.getName())).findAny();
                final String fetcherName = field.getName();
                if (endpointDirectiveOpt.isPresent()) {
                    final Directive endpointDirective = endpointDirectiveOpt.get();
                    final String endpoint = ((StringValue) endpointDirective.getArgument("endpoint").getValue()).getValue();
                    final List<String> values = getListStringArgument(endpointDirective, "values");
                    final List<String> mappings = getListStringArgument(endpointDirective, "mappings");
                    final Outbound handler = handlers.get(endpointDirective.getName());
                    final boolean isListType = isListReturnType(field);
                    DataFetcher<CompletionStage<?>> next = new OutgoingDataFetcher(endpoint, service.upstream(), handler, plugins, values, mappings, isListType);
                    for (GraphqlInboundPlugin plugin : plugins) {
                        next = plugin.fetcherChain(directives, next);
                    }
                    final DataFetcher<CompletionStage<?>> fetcher = next;
                    register.type(type, builder -> builder.dataFetcher(fetcherName, new AccessLogFetcher(fetcherName, fetcher)));
                } else {
                    register.type(type, builder -> builder.dataFetcher(fetcherName, new AccessLogFetcher(fetcherName, new NotFoundFetcher())));
                }
            });
        };
        bind.accept("Query");
        bind.accept("Mutation");
    }

    private List<String> getListStringArgument(final Directive directive, final String name) {
        final Argument argument = directive.getArgument(name);
        if (Objects.isNull(argument)) {
            return Collections.emptyList();
        }
        final ArrayValue array = (ArrayValue) argument.getValue();
        final List<String> values = new ArrayList<>(array.getValues().size());
        array.getValues().forEach(value -> values.add(((StringValue) value).getValue()));
        return values;
    }

    private boolean isListReturnType(final FieldDefinition field) {
        final Type<?> type = field.getType();
        if (type instanceof ListType) {
            return true;
        }
        final List<Node> children = type.getChildren();
        for (Node child : children) {
            if (child instanceof ListType) {
                return true;
            }
        }
        return false;
    }

}
