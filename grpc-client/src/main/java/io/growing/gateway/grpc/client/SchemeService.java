package io.growing.gateway.grpc.client;

import com.google.protobuf.Empty;
import io.growing.gateway.FileDescriptorDto;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;

public class SchemeService extends SchemeServiceGrpc.SchemeServiceImplBase {

    public static SchemeService newInstance() {
        return new SchemeService();
    }

    private final ClasspathGraphqlSchemaScanner scanner = new ClasspathGraphqlSchemaScanner();

    @Override
    public void getScheme(Empty request, StreamObserver<SchemeDto> responseObserver) {
        try {
            final List<FileDescriptorDto> files = scanner.scan("graphql");
            final SchemeDto scheme = SchemeDto.newBuilder().addAllGraphqlDefinitions(files).build();
            responseObserver.onNext(scheme);
        } catch (IOException e) {
            responseObserver.onError(e);

        }
        responseObserver.onCompleted();
    }

}
