package io.growing.gateway.discovery;

import io.growing.gateway.meta.Upstream;

import java.util.List;

/**
 * @author AI
 */
public interface ClusterDiscoveryService {

    List<Upstream> discover();

}
