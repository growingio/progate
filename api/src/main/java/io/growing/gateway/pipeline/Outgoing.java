package io.growing.gateway.pipeline;

import io.growing.gateway.meta.Upstream;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.module.ModuleScheme;

import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public interface Outgoing {

    String protocol();

    ModuleScheme load(Upstream upstream);

    CompletionStage<?> handle(Upstream upstream, String endpoint, RequestContext request);

}
