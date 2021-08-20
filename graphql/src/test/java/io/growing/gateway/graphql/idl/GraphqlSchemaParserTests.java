package io.growing.gateway.graphql.idl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.module.EndpointDefinition;
import io.growing.gateway.module.ModuleScheme;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GraphqlSchemaParserTests {

    private final ModuleScheme scheme = new ModuleScheme() {
        @Override
        public String name() {
            return null;
        }

        @Override
        public List<EndpointDefinition> graphqlDefinitions() {
            return Lists.newArrayList(
                new EndpointDefinition("test", "type TestQuery { \n test: String \n }".getBytes()),
                new EndpointDefinition("demo", "type DemoQuery { \n demo: String \n}".getBytes()),
                new EndpointDefinition("testMutation", "type Test {\n field: String \n } \ntype testMutation { \n createTest: String \n}".getBytes()),
                new EndpointDefinition("test.schema.graphql", "type TestQuery {\n field: String \n }".getBytes()),
                new EndpointDefinition("job.graphql", "type Query {\n jobs: [String] \n }".getBytes())
            );
        }

        @Override
        public List<EndpointDefinition> restfulDefinitions() {
            return null;
        }
    };

    @Test
    public void test() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser();
        final TypeDefinitionRegistry registry = parser.parse(scheme);
        assertResult(registry);
    }

    @Test
    public void testParseSchemes() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser();
        final List<ModuleScheme> schemes = Lists.newArrayList(scheme, new ModuleScheme() {
            @Override
            public String name() {
                return null;
            }

            @Override
            public List<EndpointDefinition> graphqlDefinitions() {
                return null;
            }

            @Override
            public List<EndpointDefinition> restfulDefinitions() {
                return null;
            }
        });
        final TypeDefinitionRegistry registry = parser.parse(schemes);
        assertResult(registry);
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
