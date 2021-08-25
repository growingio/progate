package io.growing.gateway.grpc.finder;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Empty;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.growing.gateway.grpc.ServiceResolveException;
import io.growing.gateway.grpc.ServiceResolver;
import io.growing.gateway.grpc.impl.FileDescriptorServiceResolver;
import io.grpc.Channel;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.stub.StreamObserver;

import java.util.Set;

public class ServiceModuleFinder {

    public SchemeDto loadScheme(final Channel channel) {
        final SchemeServiceGrpc.SchemeServiceFutureStub stub = SchemeServiceGrpc.newFutureStub(channel);
        try {
            return stub.getScheme(Empty.getDefaultInstance()).get();
        } catch (Exception e) {
            throw new ServiceResolveException("Cannot load module scheme", e);
        }
    }

    public ServiceResolver createServiceResolver(final Channel channel) {
        final ServerReflectionGrpc.ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel);
        final ServerReflectionObserver observer = new ServerReflectionObserver();
        final StreamObserver<ServerReflectionRequest> requestStreamObserver = stub.serverReflectionInfo(observer);
        observer.request(requestStreamObserver);
        try {
            final Set<DescriptorProtos.FileDescriptorProto> fileDescriptorProtos = observer.getCompletionFuture().get();
            return FileDescriptorServiceResolver.fromFileDescriptorProtoSet(fileDescriptorProtos);
        } catch (Exception e) {
            throw new ServiceResolveException("Cannot load service descriptors", e);
        }
    }

}
