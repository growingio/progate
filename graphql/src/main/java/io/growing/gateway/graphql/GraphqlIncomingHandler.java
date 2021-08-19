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
import graphql.language.StringValue;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLCodeRegistry;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.api.IncomingHandler;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.graphql.request.GraphqlRelayRequest;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.module.ModuleScheme;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class GraphqlIncomingHandler implements IncomingHandler {

    private final AtomicReference<GraphQL> graphQLReference = new AtomicReference<>();

    @Override
    public void reload(final List<Upstream> upstreams, final Set<OutgoingHandler> outgoings) {
        final Map<String, OutgoingHandler> handlers = new HashMap<>(outgoings.size());
        outgoings.forEach(handler -> handlers.put(handler.protocol(), handler));
        upstreams.forEach(upstream -> {
            final OutgoingHandler handler = handlers.get(upstream.getProtocol());
            final ModuleScheme module = handler.load(upstream);
            graphQLReference.set(createGraphql(upstream, module, handler));
        });
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
                //
                final GraphqlRelayRequest graphqlRequest = gson.fromJson(ar.result().toString(StandardCharsets.UTF_8), GraphqlRelayRequest.class);
                final GraphQLContext context = GraphQLContext.newContext().build();
                final ExecutionInput execution = ExecutionInput.newExecutionInput(graphqlRequest.getQuery()).localContext(context).build();
                final CompletableFuture<ExecutionResult> future = graphQLReference.get().executeAsync(execution);
                //
                future.whenComplete((r, t) -> {
                    if (Objects.nonNull(t)) {
                        request.response().setStatusCode(500).end("Error");
                    } else {
                        HttpServerResponse response = request.response();
                        response.headers().set(HttpHeaders.CONTENT_TYPE, "application/json;charset=utf-8");
                        try {
                            String chunk = gson.toJson(r);
                            response.end(chunk);
                        } catch (Exception e) {
                            request.response().setStatusCode(500).end("Error");
                        }
                    }
                });
            } else {
                request.response().setStatusCode(500).end("Error");
            }
        });
    }

    private GraphQL createGraphql(final Upstream upstream, final ModuleScheme scheme, final OutgoingHandler handler) {

        final TypeDefinitionRegistry registry = new SchemaParser().parse(new String());

        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();

        final Set<String> fetcherTypes = Sets.newHashSet("Query", "Mutation");
        final Set<String> endpointNames = Sets.newHashSet(upstream.getProtocol());
        registry.types().forEach((type, value) -> {
            if (fetcherTypes.contains(type)) {
                final List<FieldDefinition> definitions = ((ObjectTypeDefinition) value).getFieldDefinitions();
                definitions.forEach(definition -> {
                    try (final Stream<Directive> stream = definition.getDirectives().stream()) {
                        final Optional<Directive> endpointDirectiveOpt = stream.filter(directive -> endpointNames.contains(directive.getName())).findAny();
                        if (endpointDirectiveOpt.isPresent()) {
                            final Directive endpointDirective = endpointDirectiveOpt.get();
                            final String endpoint = ((StringValue) endpointDirective.getArgument("endpoint").getValue()).getValue();
                            final DataFetcher<CompletionStage<? extends Object>> fetcher = new OutgoingDataFetcher(endpoint, upstream, handler);
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
