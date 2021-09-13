package io.growing.gateway.grpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.util.JsonFormat;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.finder.ServiceModuleFinder;
import io.growing.gateway.grpc.interceptor.RequestLogInterceptor;
import io.growing.gateway.grpc.json.Jackson;
import io.growing.gateway.grpc.observer.CollectionObserver;
import io.growing.gateway.grpc.observer.UnaryObserver;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.meta.Upstream;
import io.growing.gateway.pipeline.Outgoing;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
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
    public CompletionStage<?> handle(Upstream upstream, String endpoint, RequestContext request) {
        final ServiceResolver resolver = resolvers.get(upstream);
        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(endpoint);
        final MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor = resolver.resolveMethod(methodDescriptor);
        final Channel channel = createChannel(upstream, request);
        final ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(grpcMethodDescriptor, CallOptions.DEFAULT);
        DynamicMessage message;
        try {
            message = transcode(request, methodDescriptor.getInputType(), resolver.getTypeDescriptors());
        } catch (Exception e) {
            final CompletableFuture<?> future = new CompletableFuture<>();
            future.completeExceptionally(e);
            return future;
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
            return observer.toCompletionStage().thenApply(dm -> {
                if (methodDescriptor.getOutputType().getFullName().equals(Empty.getDescriptor().getFullName())) {
                    return true;
                } else {
                    return new DynamicMessageWrapper(dm, resolver.getTypeDescriptors());
                }
            });
        }
    }

    private DynamicMessage transcode(final RequestContext context, final Descriptors.Descriptor type,
                                     final Set<Descriptors.Descriptor> descriptors) throws InvalidProtocolBufferException, JsonProcessingException {
        final DynamicMessage.Builder builder = DynamicMessage.newBuilder(type);
        final String json = Jackson.MAPPER.writeValueAsString(context.getArguments());
        final TypeRegistry.Builder tr = TypeRegistry.newBuilder();
        descriptors.forEach(tr::add);
        JsonFormat.parser().ignoringUnknownFields().usingTypeRegistry(tr.build()).merge(json, builder);
        return builder.build();
    }

    private ServiceResolver createServiceResolver(final Upstream upstream) {
        final ManagedChannel channel = ChannelFactory.get(upstream, null);
        return finder.createServiceResolver(channel);
    }

    private Channel createChannel(final Upstream upstream, final RequestContext request) {
        final ManagedChannel origin = ChannelFactory.get(upstream, request);
        final RequestLogInterceptor interceptor = new RequestLogInterceptor(request.id());
        return ClientInterceptors.intercept(origin, interceptor);
    }

}
