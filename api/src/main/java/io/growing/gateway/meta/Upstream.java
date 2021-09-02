package io.growing.gateway.meta;

import io.growing.gateway.cluster.LoadBalance;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author AI
 */
public interface Upstream {

    boolean isInternal();

    String name();

    String protocol();

    List<ServerNode> nodes();

    LoadBalance balancer();

    default List<ServerNode> getAvailableNodes() {
        return nodes().stream().filter(ServerNode::isAvailable).collect(Collectors.toList());
    }

}
