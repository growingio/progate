package io.growing.gateway.graphql.idl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.growing.gateway.cluster.LoadBalance;
import io.growing.gateway.meta.EndpointDefinition;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

class GraphqlSchemaParserTest {

    private final Upstream upstream = new Upstream() {
        @Override
        public String name() {
            return "demo";
        }

        @Override
        public String protocol() {
            return null;
        }

        @Override
        public boolean isInternal() {
            return false;
        }

        @Override
        public LoadBalance balancer() {
            return null;
        }

        @Override
        public List<ServerNode> nodes() {
            return null;
        }
    };

    private final ServiceMetadata service = new ServiceMetadata() {
        @Override
        public Upstream upstream() {
            return upstream;
        }

        @Override
        public List<EndpointDefinition> graphqlDefinitions() {
            return Lists.newArrayList(
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
        }

        @Override
        public List<EndpointDefinition> restfulDefinitions() {
            return null;
        }
    };

    @Test
    void test() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(Collections.emptySet());
        final TypeDefinitionRegistry registry = parser.parse(service);
        assertResult(registry);
    }

    @Test
    void testParseSchemes() {
        final GraphqlSchemaParser parser = new GraphqlSchemaParser(Collections.emptySet());
        final List<ServiceMetadata> services = Lists.newArrayList(service, new ServiceMetadata() {
            @Override
            public Upstream upstream() {
                return upstream;
            }

            @Override
            public List<EndpointDefinition> graphqlDefinitions() {
                return Lists.newArrayList(
                    new EndpointDefinition("b.schema.graphql", "scalar Long \n ".getBytes())
                );
            }

            @Override
            public List<EndpointDefinition> restfulDefinitions() {
                return null;
            }
        });
        final TypeDefinitionRegistry registry = parser.parse(services);
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
