package io.growing.gateway.grpc.client;

import com.google.protobuf.Empty;
import io.growing.gateway.FileDescriptorDto;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.List;

public class SchemeService extends SchemeServiceGrpc.SchemeServiceImplBase {

    private ClassLoader[] classLoaders;

    public static SchemeService newInstance() {
        return new SchemeService();
    }

    private final ClasspathGraphqlSchemaScanner graphqlSchemaScanner = new ClasspathGraphqlSchemaScanner();
    private final ClasspathOpenApiSchemaScanner openApiSchemaScanner = new ClasspathOpenApiSchemaScanner();

    public SchemeService() {
        this.classLoaders = new ClassLoader[]{this.getClass().getClassLoader()};
    }

    public SchemeService(ClassLoader[] classLoaders) {
        this.classLoaders = classLoaders;
    }

    @Override
    public void getScheme(Empty request, StreamObserver<SchemeDto> responseObserver) {
        try {
            final List<FileDescriptorDto> graphqlFiles = graphqlSchemaScanner.scan(this.classLoaders,"graphql");
            final List<FileDescriptorDto> restfulFiles = openApiSchemaScanner.scan(this.classLoaders,"restful");
            final SchemeDto scheme = SchemeDto.newBuilder().addAllRestfulDefinitions(restfulFiles).addAllGraphqlDefinitions(graphqlFiles).build();
            responseObserver.onNext(scheme);
        } catch (IOException e) {
            responseObserver.onError(e);
        }
        responseObserver.onCompleted();
    }

}
