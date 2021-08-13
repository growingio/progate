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

    }


}
