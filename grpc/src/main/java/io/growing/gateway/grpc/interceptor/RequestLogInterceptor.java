package io.growing.gateway.grpc.interceptor;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestLogInterceptor implements ClientInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLogInterceptor.class);
    private final String requestId;

    public RequestLogInterceptor(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                final long started = System.nanoTime();
                final String methodType = method.getType().name();
                final String fullMethodName = method.getFullMethodName();
                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("{} {} {}", requestId, methodType, fullMethodName);
                }
                final Metadata.Key<String> header = Metadata.Key.of("x-request-id", Metadata.ASCII_STRING_MARSHALLER);
                headers.put(header, requestId);
                super.start(new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(responseListener) {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        final double cost = (System.nanoTime() - started) / 1000000.0;
                        if (LOGGER.isInfoEnabled()) {
                            LOGGER.info("{} {} {} {} cost: {}", requestId, methodType, fullMethodName, status.getCode(), cost);
                        }
                        super.onClose(status, trailers);
                    }
                }, headers);
            }
        };
    }

}
