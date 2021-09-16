package io.growing.gateway.meta;

import io.growing.gateway.cluster.ClusterStateException;
import io.growing.gateway.cluster.LoadBalance;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public interface Upstream {

    String name();

    String protocol();

    boolean isInternal();

    LoadBalance balancer();

    List<ServerNode> nodes();

    default List<ServerNode> getAvailableNodes() {
        final List<ServerNode> availableNodes = nodes().stream().filter(ServerNode::isAvailable).collect(Collectors.toList());
        if (availableNodes.isEmpty()) {
            throw new ClusterStateException("Unreachable cluster " + name());
        }
        return availableNodes;
    }

}
