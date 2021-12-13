package io.growing.gateway.progate.idl;

import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.relay.Relay;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.idl.GlobalIdSchemaDirectiveWiring;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class GlobalIdSchemaDirectiveWiringTests {

    @Test
    public void test() {
        final String id = "abc";
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        runtimeWiringBuilder.directive(GlobalIdSchemaDirectiveWiring.NAME, new GlobalIdSchemaDirectiveWiring());
        runtimeWiringBuilder.type("Query", builder -> builder.dataFetcher("job", environment -> {
            final Map<String, String> job = new HashMap<>();
            job.put("id", id);
            return job;
        }));
        final TypeDefinitionRegistry registry = new SchemaParser().parse("directive @globalId on FIELD_DEFINITION\n type Job { id: String @globalId } \n type Query { job: Job}");
        final SchemaGenerator generator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        final GraphQL graphql = GraphQL.newGraphQL(graphQLSchema).build();
        final ExecutionResult result = graphql.execute("query {job { id }}");
        final Map<String, Map<String, String>> job = result.getData();
        Assertions.assertEquals(new Relay().toGlobalId("Job", id), job.get("job").get("id"));
    }
}
