package io.growing.gateway.graphql;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.GraphQLContext;
import graphql.language.Directive;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.api.IncomingHandler;
import io.growing.gateway.discovery.UpstreamDiscovery;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.graphql.request.GraphqlRelayRequest;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class GraphqlIncomingHandler implements IncomingHandler {

    private EventBus eventBus;
    private GraphqlSchemaScanner scanner;
    private UpstreamDiscovery upstreamDiscovery;
    private final AtomicReference<GraphQL> graphqlRef = new AtomicReference<>();

    public void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void setScanner(GraphqlSchemaScanner scanner) {
        this.scanner = scanner;
        graphqlRef.set(createGraphql());
    }

    public void setUpstreamDiscovery(UpstreamDiscovery upstreamDiscovery) {
        this.upstreamDiscovery = upstreamDiscovery;
    }

    @Override
    public Set<HttpApi> apis() {
        final HttpApi httpApi = new HttpApi();
        httpApi.setPath("/graphql");
        httpApi.setMethods(Sets.newHashSet(HttpMethod.POST));
        return Sets.newHashSet(httpApi);
    }

    @Override
    public void handle(HttpServerRequest request) {
        request.body(ar -> {
            if (ar.succeeded()) {
                final Gson gson = new Gson();
                final GraphqlRelayRequest graphqlRequest = gson.fromJson(ar.result().toString(StandardCharsets.UTF_8), GraphqlRelayRequest.class);
                final GraphQLContext context = GraphQLContext.newContext().of("r", "Hello").build();
                final ExecutionInput execution = ExecutionInput.newExecutionInput(graphqlRequest.getQuery()).localContext(context).build();
                final CompletableFuture<ExecutionResult> future = graphqlRef.get().executeAsync(execution);
                future.whenComplete((r, t) -> {
                    HttpServerResponse response = request.response();
                    response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                    response.end(gson.toJson(r));
                });
            } else {
                request.response().setStatusCode(500).end("Error");
            }
        });
    }

    private GraphQL createGraphql() {

        final TypeDefinitionRegistry registry = new SchemaParser().parse(scanner.scan(null));
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();

        final Set<String> fetcherTypes = Sets.newHashSet("Query", "Mutation");
        final Set<String> endpointNames = Sets.newHashSet("grpc", "dubbo");
        registry.types().forEach((type, value) -> {
            if (fetcherTypes.contains(type)) {
                final List<FieldDefinition> definitions = ((ObjectTypeDefinition) value).getFieldDefinitions();
                definitions.forEach(definition -> {
                    try (final Stream<Directive> stream = definition.getDirectives().stream()) {
                        final Optional<Directive> endpointDirectiveOpt = stream.filter(directive -> endpointNames.contains(directive.getName())).findAny();
                        if (endpointDirectiveOpt.isPresent()) {
                            final Directive endpointDirective = endpointDirectiveOpt.get();
                            final DataFetcher<Object> fetcher = new OutgoingDataFetcher(endpointDirective.getName(), endpointDirective.getArgument("endpoint").getValue().toString());
                            runtimeWiringBuilder.type(type, builder -> builder.dataFetcher(definition.getName(), fetcher));
                        } else {
                            // fixme: add not found
                        }
                    }
                });
            }
        });

        final SchemaGenerator generator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        return GraphQL.newGraphQL(graphQLSchema).build();
    }


}
