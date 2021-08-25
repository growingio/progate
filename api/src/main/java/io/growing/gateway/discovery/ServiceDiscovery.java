package io.growing.gateway.discovery;

import io.growing.gateway.meta.Upstream;
import io.growing.gateway.meta.ServiceMetadata;

public interface ServiceDiscovery {

    ServiceMetadata discover(Upstream upstream);

}
