package io.growing.gateway.pipeline;

import java.util.concurrent.CompletionStage;

public interface ServiceCall<RespT> {

    String endpoint();

    String methodName();

    String fullServiceName();

    Request request();

    CompletionStage<Response> call();

}
