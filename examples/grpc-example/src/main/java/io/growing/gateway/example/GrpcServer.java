package io.growing.gateway.example;

import io.growing.gateway.grpc.client.SchemeService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

/**
 * @author AI
 */
public class GrpcServer {

    public static void main(final String[] args) throws Exception {
        final int port = 18080;
        final Server server = ServerBuilder.forPort(port)
            .addService(SchemeService.newInstance())
            .addService(ProtoReflectionService.newInstance())
            .addService(new JobServiceImpl())
            .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdownNow()));
        server.start();
        System.out.println("Server started, listening on " + port);
        server.awaitTermination();
    }

}
