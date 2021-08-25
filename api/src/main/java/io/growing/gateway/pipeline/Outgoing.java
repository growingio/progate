package io.growing.gateway.pipeline;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.meta.Upstream;

import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public interface Outgoing {

    String protocol();

    CompletionStage<?> handle(Upstream upstream, String endpoint, RequestContext request);

}
