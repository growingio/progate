package io.growing.gateway.internal;

import com.google.common.collect.Lists;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.api.UpstreamNode;
import io.growing.gateway.discovery.UpstreamDiscovery;

import java.util.List;

/**
 * @author AI
 */
public class ConfigUpstreamDiscovery implements UpstreamDiscovery {
    @Override
    public List<Upstream> discover() {
        final Upstream upstream = new Upstream();
        final UpstreamNode node = new UpstreamNode();
        node.setHost("localhost");
        node.setPort(18080);
        upstream.setName("demo");
        upstream.setProtocol("grpc");
        upstream.setNodes(new UpstreamNode[]{node});
        return Lists.newArrayList(upstream);
    }
}
