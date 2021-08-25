package io.growing.gateway.grpc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.gson.Gson;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.finder.ServiceModuleFinder;
import io.growing.gateway.grpc.observer.CollectionObserver;
import io.growing.gateway.grpc.observer.UnaryObserver;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.module.ModuleScheme;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public class GrpcOutgoing implements Outgoing {

    private final ServiceModuleFinder finder = new ServiceModuleFinder();
    private final LoadingCache<Upstream, ServiceResolver> resolvers = Caffeine.newBuilder().build(this::createServiceResolver);

    @Override
    public String protocol() {
        return "grpc";
    }

    @Override
    public ModuleScheme load(Upstream upstream) {
        final ManagedChannel channel = finder.createChannel(upstream);
        return finder.loadScheme(channel);
    }

    @Override
    public CompletionStage<?> handle(Upstream upstream, String endpoint, RequestContext request) {
        final ServiceResolver resolver = resolvers.get(upstream);
        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(endpoint);
        final MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor = resolver.resolveMethod(methodDescriptor);
        final ServerNode node = upstream.getNodes()[0];
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
                    wrappers.add(new DynamicMessageWrapper(dm, resolver.getTypeDescriptors()));
                }
                return wrappers;
            });
        } else {
            final UnaryObserver<DynamicMessage> observer = new UnaryObserver<>();
            ClientCalls.asyncUnaryCall(call, message, observer);
            return observer.toCompletionStage().thenApply(dm -> new DynamicMessageWrapper(dm, resolver.getTypeDescriptors()));
        }
    }

    private DynamicMessage transcode(final RequestContext context, final Descriptors.Descriptor type) throws InvalidProtocolBufferException {
        final DynamicMessage.Builder builder = DynamicMessage.newBuilder(type);
        final String json = new Gson().toJson(context.getArguments());
        JsonFormat.parser().ignoringUnknownFields().merge(json, builder);
        return builder.build();
    }

    private ServiceResolver createServiceResolver(final Upstream upstream) {
        final ManagedChannel channel = finder.createChannel(upstream);
        return finder.createServiceResolver(channel);
    }

}
