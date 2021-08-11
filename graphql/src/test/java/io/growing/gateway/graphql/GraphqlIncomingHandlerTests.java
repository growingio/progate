package io.growing.gateway.graphql;

import com.google.common.collect.Sets;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Directive;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.fetcher.OutgoingDataFetcher;
import io.growing.gateway.graphql.internal.ClassPathGraphqlSchemaScanner;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class GraphqlIncomingHandlerTests {

    @Test
    public void test() {
        final GraphqlSchemaScanner scanner = new ClassPathGraphqlSchemaScanner("/graphql/all.graphql");
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
        final GraphQL graphql = GraphQL.newGraphQL(graphQLSchema).build();

        ExecutionResult execute = graphql.execute("{ jobs { name } }");
        Object data = execute.getData();
        System.out.println(data);
    }


}
