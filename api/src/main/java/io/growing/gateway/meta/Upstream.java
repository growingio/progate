package io.growing.gateway.meta;

import io.growing.gateway.cluster.LoadBalance;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author AI
 */
public interface Upstream {

    String name();

    String protocol();

    List<ServerNode> nodes();

    LoadBalance balancer();

    default List<ServerNode> getAvailableNodes() {
        try (Stream<ServerNode> stream = nodes().stream()) {
            return stream.filter(ServerNode::isAvailable).collect(Collectors.toList());
        }
    }

}
