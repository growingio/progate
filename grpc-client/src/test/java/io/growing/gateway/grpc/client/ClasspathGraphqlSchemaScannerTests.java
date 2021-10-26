package io.growing.gateway.grpc.client;

import io.growing.gateway.FileDescriptorDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ClasspathGraphqlSchemaScannerTests {

    @Test
    public void test() throws IOException {
        final ClasspathGraphqlSchemaScanner scanner = new ClasspathGraphqlSchemaScanner();
        final List<FileDescriptorDto> files = scanner.scan(new ClassLoader[]{this.getClass().getClassLoader()}, "graphql");

        Assertions.assertEquals(1, files.size());
        Assertions.assertEquals("graphql/empty.graphql", files.get(0).getName());
    }

}
