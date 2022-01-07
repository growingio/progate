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

import java.util.List;
import java.util.Set;

public class ServiceModuleFinder {

    public SchemeDto loadScheme(final TaggedChannel channel) {
        final SchemeServiceGrpc.SchemeServiceFutureStub stub = SchemeServiceGrpc.newFutureStub(channel.getChannel());
        try {
            return stub.getScheme(Empty.getDefaultInstance()).get();
        } catch (Exception e) {
            final String message = String.format("Cannot load module scheme on node %s:%d", channel.getNode().host(), channel.getNode().port());
            throw new ServiceResolveException(message, e);
        }
    }

    public ServiceResolver createServiceResolver(final TaggedChannel channel) {
        final ServerReflectionGrpc.ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel.getChannel());
        final ServerReflectionObserver observer = new ServerReflectionObserver();
        final StreamObserver<ServerReflectionRequest> requestStreamObserver = stub.serverReflectionInfo(observer);
        observer.request(requestStreamObserver);
        try {
            final List<DescriptorProtos.FileDescriptorProto> fileDescriptorProtos = observer.getCompletionFuture().get();
            return FileDescriptorServiceResolver.fromFileDescriptorProtoSet(fileDescriptorProtos);
        } catch (Exception e) {
            final String message = String.format("Cannot load service descriptors on node %s:%d", channel.getNode().host(), channel.getNode().port());
            throw new ServiceResolveException(message, e);
        }
    }

}
