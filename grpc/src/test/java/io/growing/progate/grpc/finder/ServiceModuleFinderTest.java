package io.growing.progate.grpc.finder;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Empty;
import io.growing.gateway.FileDescriptorDto;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.growing.gateway.grpc.ServiceResolver;
import io.growing.gateway.grpc.finder.ServiceModuleFinder;
import io.growing.gateway.grpc.finder.TaggedChannel;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ServiceModuleFinderTest {

    private static Server server;
    private static String serverName;
    private static final String GRAPHQL_NAME = "empty.graphql";

    @BeforeAll
    public static void startup() throws IOException {
        serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
            .addService(ProtoReflectionService.newInstance())
            .addService(new SchemeServiceGrpc.SchemeServiceImplBase() {
                @Override
                public void getScheme(Empty request, StreamObserver<SchemeDto> responseObserver) {
                    responseObserver.onNext(SchemeDto.newBuilder().addGraphqlDefinitions(FileDescriptorDto.newBuilder().setName(GRAPHQL_NAME)).build());
                    responseObserver.onCompleted();
                }
            }).build();
        server.start();
    }

    @AfterAll
    public static void destroy() {
        server.shutdown();
        server = null;
    }

    @Test
    void testLoadScheme() {
        final ServiceModuleFinder finder = new ServiceModuleFinder();
        final TaggedChannel channel = TaggedChannel.from(InProcessChannelBuilder.forName(serverName).build(), null);
        final SchemeDto scheme = finder.loadScheme(channel);
        Assertions.assertEquals(1, scheme.getGraphqlDefinitionsCount());
        Assertions.assertEquals(GRAPHQL_NAME, scheme.getGraphqlDefinitions(0).getName());
    }

    @Test
    void testCreateServiceResolver() {
        final ServiceModuleFinder finder = new ServiceModuleFinder();
        final TaggedChannel channel = TaggedChannel.from(InProcessChannelBuilder.forName(serverName).build(), null);
        final ServiceResolver resolver = finder.createServiceResolver(channel);
        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(SchemeServiceGrpc.getGetSchemeMethod().getFullMethodName());
        Assertions.assertNotNull(methodDescriptor);
        Assertions.assertEquals("growing.gateway.SchemeService.GetScheme", methodDescriptor.getFullName());
    }


}
