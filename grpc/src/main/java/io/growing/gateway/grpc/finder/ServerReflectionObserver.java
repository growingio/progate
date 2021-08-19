package io.growing.gateway.grpc.finder;

import io.grpc.reflection.v1alpha.ServerReflectionRequest;
import io.grpc.reflection.v1alpha.ServerReflectionResponse;
import io.grpc.stub.StreamObserver;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ServerReflectionObserver extends FileDescriptorProtoSetObserver {

    private StreamObserver<ServerReflectionRequest> requestStreamObserver;

    @Override
    public void onNext(ServerReflectionResponse value) {
        if (value.hasListServicesResponse() && Objects.nonNull(requestStreamObserver)) {
            value.getListServicesResponse().getServiceList().forEach(sr -> {
                final ServerReflectionRequest request = ServerReflectionRequest.newBuilder().setFileContainingSymbol(sr.getName()).build();
                requestStreamObserver.onNext(request);
            });
            requestStreamObserver.onCompleted();
        } else {
            super.onNext(value);
        }
    }

    public void request(final StreamObserver<ServerReflectionRequest> requestStreamObserver) {
        this.requestStreamObserver = requestStreamObserver;
        final ServerReflectionRequest request = ServerReflectionRequest.newBuilder().setListServices(StringUtils.EMPTY).build();
        requestStreamObserver.onNext(request);
    }

}
