package io.growing.gateway.graphql.plugin;

import graphql.language.Directive;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLScalarType;
import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.vertx.core.http.HttpServerRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionStage;

public interface GraphqlInboundPlugin {

    void init(GraphqlConfig config, RuntimeContext context);

    default Set<SchemaObjectResolver> resolvers() {
        return Collections.emptySet();
    }

    default Map<String, Object> arguments(HttpServerRequest request) {
        return Collections.emptyMap();
    }

    default DataFetcher<CompletionStage<?>> fetcherChain(List<Directive> directions, DataFetcher<CompletionStage<?>> next) {
        return next;
    }

    default Object wrapResult(Object value) {
        return value;
    }

    default void transcodeArguments(DataFetchingEnvironment environment, Map<String, Object> arguments) {
    }

    default Set<GraphQLScalarType> scalars() {
        return Collections.emptySet();
    }

}
