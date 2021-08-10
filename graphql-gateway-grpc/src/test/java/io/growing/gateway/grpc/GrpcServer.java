package io.growing.gateway.grpc;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.UpstreamServiceGrpc;
import io.grpc.Server;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;

import java.io.FileInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author AI
 */
public class GrpcServer {

    private Server server = null;

    String getServerName() {
        return "test-in-process";
    }

    void start() throws Exception {
        try {
            server = InProcessServerBuilder.forName(getServerName())
                .addService(ProtoReflectionService.newInstance())
                .addService(new UpstreamServiceGrpc.UpstreamServiceImplBase() {
                    @Override
                    public void getScheme(Empty request, StreamObserver<SchemeDto> responseObserver) {
                        try {
                            SchemeDto.Builder schemeDtoBuilder = SchemeDto.newBuilder();
                            URI uri = getClass().getResource("/graphql").toURI();
                            Path graphqlPath = Paths.get(uri);
                            Stream<Path> pathStream = Files.walk(graphqlPath);
                            pathStream.forEach(path -> {
                                try {
                                    FileInputStream fileInputStream = new FileInputStream(path.toFile());
                                    schemeDtoBuilder.addGraphqlDefinitions(ByteString.readFrom(fileInputStream));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });

                            responseObserver.onNext(schemeDtoBuilder.build());
                            responseObserver.onCompleted();
                        } catch (Exception e) {
                            e.printStackTrace();
                            responseObserver.onError(e);
                        } finally {
                            responseObserver.onCompleted();
                        }
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
            server.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stop() {
        try {
            server.shutdownNow();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
