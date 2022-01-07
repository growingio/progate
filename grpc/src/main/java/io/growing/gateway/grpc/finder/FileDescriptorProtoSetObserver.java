package io.growing.gateway.grpc.finder;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;

import java.util.List;

/**
 * @author AI
 */
public class FileDescriptorProtoSetObserver implements StreamObserver<ServerReflectionResponse> {

    private final SettableFuture<List<DescriptorProtos.FileDescriptorProto>> future;
    private final ImmutableList.Builder<DescriptorProtos.FileDescriptorProto> fileDescriptorSet;

    public FileDescriptorProtoSetObserver() {
        this.future = SettableFuture.create();
        this.fileDescriptorSet = ImmutableList.builder();
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

    public ListenableFuture<List<DescriptorProtos.FileDescriptorProto>> getCompletionFuture() {
        return future;
    }

}
