package io.growing.gateway.grpc.discovery;

import io.growing.gateway.SchemeDto;
import io.growing.gateway.discovery.ServiceDiscoveryService;
import io.growing.gateway.grpc.ChannelFactory;
import io.growing.gateway.grpc.dto.GrpcServiceMetadata;
import io.growing.gateway.grpc.finder.ServiceModuleFinder;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.meta.Upstream;

public class GrpcReflectionServiceDiscovery implements ServiceDiscoveryService {

    private final ServiceModuleFinder finder = new ServiceModuleFinder();

    @Override
    public ServiceMetadata discover(Upstream upstream) {
        final SchemeDto scheme = finder.loadScheme(ChannelFactory.get(upstream, null));
        return GrpcServiceMetadata.form(scheme, upstream);
    }

}
