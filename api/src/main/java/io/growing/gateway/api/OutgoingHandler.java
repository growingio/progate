package io.growing.gateway.api;

import io.vertx.core.Future;

/**
 * @author AI
 */
public interface OutgoingHandler<ReqT, RespT> {

    Protocol type();

    Future<RespT> handle(String endpoint, ReqT req);

}
