package io.growing.gateway.progate.idl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.graphql.idl.GraphqlSchemaParser;
import io.growing.gateway.meta.EndpointDefinition;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphqlSchemaParserTest {

    private static final Upstream UPSTREAM = Mockito.mock(Upstream.class);
    private static final ServiceMetadata SERVICE = Mockito.mock(ServiceMetadata.class);

    @BeforeAll
    static void init() {
        Mockito.when(UPSTREAM.name()).thenReturn("demo");
        final ArrayList<EndpointDefinition> endpointDefinitions = Lists.newArrayList(
            new EndpointDefinition("test", "type TestQuery { \n test: String \n }".getBytes()),
            new EndpointDefinition("demo", "type DemoQuery { \n demo: String \n}".getBytes()),
            new EndpointDefinition("testMutation", "type Test {\n field: String \n } \ntype testMutation { \n createTest: String \n}".getBytes()),
            new EndpointDefinition("test.schema.graphql", "type TestQuery {\n field: String \n }".getBytes()),
            new EndpointDefinition("job.graphql", "type Query {\n jobs: [String] \n }".getBytes()),
            new EndpointDefinition("empty.query.graphql", "type Query { \n }".getBytes()),
            new EndpointDefinition("empty.mutation.graphql", "type Mutation {\n }".getBytes()),
            new EndpointDefinition("b.schema.graphql", "scalar Long\n".getBytes()),
            new EndpointDefinition("a.schema.graphql", "scalar Long  \n".getBytes())
        );
        Mockito.when(SERVICE.upstream()).thenReturn(UPSTREAM);
        Mockito.when(SERVICE.graphqlDefinitions()).thenReturn(endpointDefinitions);
    }

    @Test
    void test() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(Collections.emptySet());
        final TypeDefinitionRegistry registry = parser.parse(SERVICE);
        assertResult(registry);
    }

    @Test
    void testParseSchemes() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(Collections.emptySet());
        final ServiceMetadata duplicateService = Mockito.mock(ServiceMetadata.class);
        Mockito.when(duplicateService.upstream()).thenReturn(UPSTREAM);
        Mockito.when(duplicateService.graphqlDefinitions()).thenReturn(Lists.newArrayList(
            new EndpointDefinition("b.schema.graphql", "scalar Long \n ".getBytes())
        ));
        final List<ServiceMetadata> services = Lists.newArrayList(SERVICE, duplicateService);
        final TypeDefinitionRegistry registry = parser.parse(services);
        assertResult(registry);
    }

    @Test
    void testCreateGraphQLSchema() {
        final RuntimeWiring.Builder runtimeWiringBuilder = RuntimeWiring.newRuntimeWiring();
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(Collections.emptySet());
        final ServiceMetadata service = Mockito.mock(ServiceMetadata.class);
        Mockito.when(service.upstream()).thenReturn(UPSTREAM);
        Mockito.when(service.graphqlDefinitions()).thenReturn(Lists.newArrayList(
            new EndpointDefinition("empty-mutation.graphql", "type EmQuery { \n get: String \n}".getBytes())
        ));
        final TypeDefinitionRegistry registry = parser.parse(Lists.newArrayList(service));
        final SchemaGenerator generator = new SchemaGenerator();
        Assertions.assertDoesNotThrow(() -> {
            generator.makeExecutableSchema(registry, runtimeWiringBuilder.build());
        });
    }

    private void assertResult(final TypeDefinitionRegistry registry) {
        final Map<String, TypeDefinition> types = registry.types();
        Assertions.assertTrue(types.containsKey("Test"));
        Assertions.assertTrue(types.containsKey("TestQuery"));
        Assertions.assertTrue(types.containsKey("Query"));
        Assertions.assertTrue(types.containsKey("Mutation"));
        ObjectTypeDefinition query = (ObjectTypeDefinition) types.get("Query");
        final List<FieldDefinition> fieldDefinitions = query.getFieldDefinitions();
        Assertions.assertEquals(3, fieldDefinitions.size());
        final Set<String> queryNames = Sets.newHashSet("test", "demo", "jobs");
        fieldDefinitions.forEach(def -> {
            Assertions.assertTrue(queryNames.contains(def.getName()));
        });
    }

}
