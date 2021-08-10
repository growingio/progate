package io.growing.gateway.grpc;

import com.google.protobuf.Empty;
import io.growing.gateway.GatewayDto;
import io.growing.gateway.UpstreamServiceGrpc;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;

/**
 * @author AI
 */
public class GrpcServer {

    public static void main(final String[] args) {

        try {
            final Server server = ServerBuilder.forPort(18080)
                .addService(ProtoReflectionService.newInstance())
                .addService(new UpstreamServiceGrpc.UpstreamServiceImplBase() {
                    @Override
                    public void getScheme(Empty request, StreamObserver<GatewayDto> responseObserver) {
                        GatewayDto hello = GatewayDto.newBuilder().setKey("hello").build();
                        responseObserver.onNext(hello);
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    server.shutdownNow();
                }
            });
            server.awaitTermination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
