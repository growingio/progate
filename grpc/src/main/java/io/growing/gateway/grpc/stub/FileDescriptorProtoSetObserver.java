package io.growing.gateway.grpc.stub;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

import java.util.Set;

/**
 * @author AI
 */
public class FileDescriptorProtoSetObserver implements StreamObserver<ServerReflectionResponse> {

    private final SettableFuture<Set<DescriptorProtos.FileDescriptorProto>> future;
    private final ImmutableSet.Builder<DescriptorProtos.FileDescriptorProto> fileDescriptorSet;

    public FileDescriptorProtoSetObserver() {
        this.future = SettableFuture.create();
        this.fileDescriptorSet = ImmutableSet.builder();
    }

    @Override
    public void onNext(ServerReflectionResponse value) {
        try {
            for (ByteString byteString : value.getFileDescriptorResponse().getFileDescriptorProtoList()) {
                final DescriptorProtos.FileDescriptorProto fileDescriptorProto = DescriptorProtos.FileDescriptorProto.parseFrom(byteString);
                fileDescriptorSet.add(fileDescriptorProto);
            }
        } catch (InvalidProtocolBufferException e) {
            future.setException(e);
        }
    }

    @Override
    public void onError(Throwable t) {
        future.setException(t);
    }

    @Override
    public void onCompleted() {
        future.set(fileDescriptorSet.build());
    }

    public ListenableFuture<Set<DescriptorProtos.FileDescriptorProto>> getCompletionFuture() {
        return future;
    }

}
