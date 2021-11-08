package io.growing.gateway.grpc.client;

import io.growing.gateway.FileDescriptorDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class ClasspathGraphqlSchemaScannerTests {

    @Test
    void test() throws IOException {
        final List<FileDescriptorDto> files = ClasspathSchemaScanner.GRAPHQL.scan("graphql");

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("graphql/empty.graphql", files.get(0).getName());
    }

}
