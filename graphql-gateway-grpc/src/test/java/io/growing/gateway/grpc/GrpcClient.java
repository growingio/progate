package io.growing.gateway.grpc;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Empty;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.growing.gateway.grpc.impl.FileDescriptorServiceResolver;
import io.growing.gateway.grpc.stub.FileDescriptorProtoSetObserver;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import io.grpc.reflection.v1alpha.ServerReflectionGrpc;
import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;

import java.util.Set;
import java.util.concurrent.CountDownLatch;


/**
 * @author AI
 */
public class GrpcClient {

    public static void main(final String[] args) throws Exception {
        final ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 18080).usePlaintext().build();
        final ServerReflectionGrpc.ServerReflectionStub stub = ServerReflectionGrpc.newStub(channel);
        final FileDescriptorProtoSetObserver observer = new FileDescriptorProtoSetObserver();
        StreamObserver<ServerReflectionRequest> requestStreamObserver = stub.serverReflectionInfo(observer);
        final String fullServiceName = "growing.graphql.gateway.UpstreamService";
        final ServerReflectionRequest request = ServerReflectionRequest.newBuilder().setFileContainingSymbol(fullServiceName).build();
        requestStreamObserver.onNext(request);
        requestStreamObserver.onCompleted();

        final Set<DescriptorProtos.FileDescriptorProto> fileDescriptorProtoSet = observer.getCompletionFuture().get();
        final ServiceResolver resolver = FileDescriptorServiceResolver.fromFileDescriptorProtoSet(fileDescriptorProtoSet);


        final Descriptors.MethodDescriptor methodDescriptor = resolver.getMethodDescriptor(fullServiceName, "GetScheme");
        final MethodDescriptor<DynamicMessage, DynamicMessage> grpcMethodDescriptor = resolver.resolveMethod(methodDescriptor);
        try {

            final ClientCall<DynamicMessage, DynamicMessage> clientCall = channel.newCall(grpcMethodDescriptor, CallOptions.DEFAULT);
            final DynamicMessage message = DynamicMessage.parseFrom(methodDescriptor.getInputType(), Empty.newBuilder().build().toByteArray());
            final CountDownLatch clientCallLatch = new CountDownLatch(1);
            ClientCalls.asyncUnaryCall(clientCall, message, new StreamObserver<DynamicMessage>() {
                @Override
                public void onNext(DynamicMessage value) {
                    try {
                        System.out.println(JsonFormat.printer().print(value));
                    } catch (InvalidProtocolBufferException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(Throwable t) {
                    t.printStackTrace();
                    clientCallLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    clientCallLatch.countDown();
                }
            });
            clientCallLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
