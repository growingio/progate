package io.growing.gateway.grpc;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import io.growing.gateway.api.OutgoingHandler;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.api.UpstreamNode;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.impl.FileDescriptorServiceResolver;
import io.growing.gateway.grpc.internal.ClassPathResource;
import io.growing.gateway.grpc.observer.CollectionObserver;
import io.growing.gateway.grpc.observer.FileDescriptorProtoSetObserver;
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
    public CompletionStage<? extends Object> handle(Upstream upstream, String endpoint, RequestContext request) {
        final ServiceResolver resolver = resolvers.get(upstream);
        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(endpoint);
        final MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor = resolver.resolveMethod(methodDescriptor);
        final UpstreamNode node = upstream.getNodes()[0];
        final ManagedChannel channel = ManagedChannelBuilder.forAddress(node.getHost(), node.getPort()).usePlaintext().build();
        final ClientCall<DynamicMessage, DynamicMessage> call = channel.newCall(grpcMethodDescriptor, CallOptions.DEFAULT);
        try {
            final CollectionObserver<DynamicMessage> observer = new CollectionObserver<>();
            final DynamicMessage message = DynamicMessage.parseFrom(methodDescriptor.getInputType(), Empty.newBuilder().build().toByteArray());
            ClientCalls.asyncUnaryCall(call, message, observer);
            return observer.toCompletionStage().thenApply(list -> {
                final List<Map<String, Object>> entries = new LinkedList<>();
                for (DynamicMessage dm : list) {
                    entries.add(new HashMap<>() {
                        @Override
                        public Object get(Object key) {
                            for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : dm.getAllFields().entrySet()) {
                                if (entry.getKey().getName().equals(key)) {
                                    return entry.getValue();
                                }
                            }
                            return super.get(key);
                        }
                    });
                }
                return entries;
            });
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
            return CompletableFuture.failedFuture(e);
        }
    }


//    private Future<ModuleScheme> loadScheme(final Upstream upstream) {
//        final UpstreamNode node = upstream.getNodes()[0];
//        final ManagedChannel channel = ManagedChannelBuilder.forAddress(node.getHost(), node.getPort()).usePlaintext().build();
//        final SchemeServiceGrpc.SchemeServiceStub stub = SchemeServiceGrpc.newStub(channel);
//        final UnaryObserver<SchemeDto> observer = new UnaryObserver<>();
//        stub.getScheme(Empty.newBuilder().build(), observer);
//        return observer.toFuture().map(scheme -> new ModuleScheme() {
//            @Override
//            public String name() {
//                return scheme.getName();
//            }
//
//            @Override
//            public List<byte[]> graphqlDefinitions() {
//                if (scheme.getGraphqlDefinitionsCount() > 0) {
//                    try (final Stream<ByteString> stream = scheme.getGraphqlDefinitionsList().stream()) {
//                        return stream.map(ByteString::toByteArray).collect(Collectors.toList());
//                    }
//                }
//                return Collections.emptyList();
//            }
//
//            @Override
//            public List<byte[]> restfulDefinitions() {
//                if (scheme.getRestfulDefinitionsCount() > 0) {
//                    try (final Stream<ByteString> stream = scheme.getRestfulDefinitionsList().stream()) {
//                        return stream.map(ByteString::toByteArray).collect(Collectors.toList());
//                    }
//                }
//                return Collections.emptyList();
//            }
//        });
//    }

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

}
