package io.growing.gateway.discovery;

import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;

public interface ServiceDiscovery {

    ServiceMetadata discover(Upstream upstream);

}
