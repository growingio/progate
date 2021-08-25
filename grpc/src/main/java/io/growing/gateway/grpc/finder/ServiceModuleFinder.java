package io.growing.gateway.grpc.finder;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Empty;
import io.growing.gateway.SchemeDto;
import io.growing.gateway.SchemeServiceGrpc;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.grpc.ServiceResolveException;
import io.growing.gateway.grpc.ServiceResolver;
import io.growing.gateway.grpc.dto.GrpcModuleScheme;
import io.growing.gateway.grpc.impl.FileDescriptorServiceResolver;
import io.growing.gateway.module.ModuleScheme;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.stub.StreamObserver;

import java.util.Set;

public class ServiceModuleFinder {

    public ModuleScheme loadScheme(final Channel channel) {
        final SchemeServiceGrpc.SchemeServiceFutureStub stub = SchemeServiceGrpc.newFutureStub(channel);
        try {
            final SchemeDto scheme = stub.getScheme(Empty.getDefaultInstance()).get();
            return GrpcModuleScheme.form(scheme);
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

    public ManagedChannel createChannel(final Upstream upstream) {
        final ServerNode node = upstream.getNodes()[0];
        return ManagedChannelBuilder.forAddress(node.getHost(), node.getPort()).usePlaintext().build();
    }

}
