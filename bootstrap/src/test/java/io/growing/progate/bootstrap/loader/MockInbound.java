package io.growing.progate.bootstrap.loader;

import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.HttpEndpoint;
import io.growing.gateway.pipeline.Inbound;
import io.growing.gateway.pipeline.Outbound;

import java.util.List;
import java.util.Set;

public class MockInbound implements Inbound {
    @Override
    public Set<HttpEndpoint> endpoints(List<ServiceMetadata> services, Set<Outbound> outbounds, RuntimeContext context) {
        return null;
    }
}
