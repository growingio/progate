package io.growing.gateway.pipeline;

import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.meta.ServiceMetadata;

import java.util.List;
import java.util.Set;

/**
 * @author AI
 */
public interface Inbound {

    Set<HttpEndpoint> endpoints(List<ServiceMetadata> services, Set<Outbound> outbounds, RuntimeContext context);

}
