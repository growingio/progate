package io.growing.gateway.grpc.client;

import com.google.protobuf.Empty;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class SchemeServiceTests {

    @Test
    public void test() throws IOException {
        final String serverName = SchemeServiceTests.class.getName();
        final Server server = InProcessServerBuilder.forName(serverName).addService(SchemeService.newInstance()).build();
        server.start();
        final ManagedChannel channel = InProcessChannelBuilder.forName(serverName).usePlaintext().build();
        final SchemeServiceGrpc.SchemeServiceBlockingStub stub = SchemeServiceGrpc.newBlockingStub(channel);
        final SchemeDto scheme = stub.getScheme(Empty.getDefaultInstance());
        server.shutdown();
        Assertions.assertEquals(1, scheme.getGraphqlDefinitionsCount());
    }
}
