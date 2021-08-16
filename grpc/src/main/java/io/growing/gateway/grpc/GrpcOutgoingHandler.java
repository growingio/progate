package io.growing.gateway.grpc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.api.UpstreamNode;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.impl.FileDescriptorServiceResolver;
import io.growing.gateway.grpc.internal.ClassPathResource;
import io.growing.gateway.grpc.observer.CollectionObserver;
import io.growing.gateway.grpc.observer.FileDescriptorProtoSetObserver;
import io.growing.gateway.grpc.observer.UnaryObserver;
import io.growing.gateway.module.ModuleLoader;
import io.growing.gateway.module.ModuleScheme;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * @author AI
 */
public class GrpcOutgoingHandler implements OutgoingHandler {

    private final LoadingCache<Upstream, ServiceResolver> resolvers = Caffeine.newBuilder().build(this::createServiceResolver);

    @Override
    public String protocol() {
        return "grpc";
    }

    @Override
    public ModuleLoader loader() {
        return new ModuleLoader() {
            @Override
            public ModuleScheme load(Upstream upstream) {
                return new ModuleScheme() {
                    @Override
                    public String name() {
                        return upstream.getName();
                    }

                    @Override
                    public List<byte[]> graphqlDefinitions() {
                        return Lists.newArrayList(new ClassPathResource("/graphql/all.graphql").bytes());
                    }

                    @Override
                    public List<byte[]> restfulDefinitions() {
                        return Collections.emptyList();
                    }
                };
            }
        };
    }

    @Override
    public CompletionStage<?> handle(Upstream upstream, String endpoint, RequestContext request) {
        final ServiceResolver resolver = resolvers.get(upstream);
        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(endpoint);
        final MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor = resolver.resolveMethod(methodDescriptor);
        final UpstreamNode node = upstream.getNodes()[0];
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(node.getHost(), node.getPort()).usePlaintext().build();
        final ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(grpcMethodDescriptor, CallOptions.DEFAULT);
        DynamicMessage message;
        try {
            message = transcode(request, methodDescriptor.getInputType());
        } catch (InvalidProtocolBufferException e) {
            return CompletableFuture.failedFuture(e);
        }
        if (methodDescriptor.isServerStreaming()) {
            final CollectionObserver<DynamicMessage> observer = new CollectionObserver<>();
            ClientCalls.asyncServerStreamingCall(call, message, observer);
            return observer.toCompletionStage().thenApply(collection -> {
                final List<DynamicMessageWrapper> wrappers = new LinkedList<>();
                for (DynamicMessage dm : collection) {
                    wrappers.add(new DynamicMessageWrapper(dm));
                }
                return wrappers;
            });
        } else {
            final UnaryObserver<DynamicMessage> observer = new UnaryObserver<>();
            ClientCalls.asyncUnaryCall(call, message, observer);
            return observer.toCompletionStage().thenApply(DynamicMessageWrapper::new);
        }
    }

    private DynamicMessage transcode(final RequestContext context, final Descriptors.Descriptor type) throws InvalidProtocolBufferException {
        final DynamicMessage.Builder builder = DynamicMessage.newBuilder(type);
        final String json = new Gson().toJson(context.getArguments());
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return builder.build();
    }

    private ServiceResolver createServiceResolver(final Upstream upstream) throws ExecutionException, InterruptedException {
        final UpstreamNode node = upstream.getNodes()[0];
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(node.getHost(), node.getPort()).usePlaintext().build();
        final ServerReflectionGrpc.ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel);
        final FileDescriptorProtoSetObserver observer = new FileDescriptorProtoSetObserver();
        final StreamObserver<ServerReflectionRequest> requestObserver = stub.serverReflectionInfo(observer);
        requestObserver.onNext(ServerReflectionRequest.newBuilder().setFileContainingSymbol("growing.graphql.example.JobService").build());
        requestObserver.onCompleted();
        final Set<DescriptorProtos.FileDescriptorProto> fileDescriptorProtos = observer.getCompletionFuture().get();
        return FileDescriptorServiceResolver.fromFileDescriptorProtoSet(fileDescriptorProtos);
    }

    private static class DynamicMessageWrapper extends HashMap<String, Object> {
        private final DynamicMessage message;

        public DynamicMessageWrapper(final DynamicMessage message) {
            this.message = message;
        }

        @Override
        public Object get(Object key) {
            for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
                if (entry.getKey().getName().equals(key)) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

}
