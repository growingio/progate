package io.growing.gateway.meta;

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
        return nodes().stream().filter(ServerNode::isAvailable).collect(Collectors.toList());
    }

}
