package io.growing.gateway.discovery;

import io.growing.gateway.api.Upstream;

import java.util.List;

/**
 * @author AI
 */
public interface UpstreamDiscovery {

    List<Upstream> discover();

}
