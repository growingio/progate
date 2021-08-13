package io.growing.gateway.api;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.module.ModuleLoader;

import java.util.concurrent.CompletionStage;

/**
 * @author AI
 */
public interface OutgoingHandler {

    String protocol();

    ModuleLoader loader();

    CompletionStage< ? extends Object> handle(Upstream upstream, String endpoint, RequestContext request);

}
