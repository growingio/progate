package io.growing.progate.cluster;

import io.growing.gateway.cluster.LoadBalance;
import io.growing.gateway.meta.ServerNode;
import io.growing.progate.utilities.CollectionUtilities;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobin implements LoadBalance {

    private final AtomicInteger index = new AtomicInteger(0);

    @Override
    public ServerNode select(List<ServerNode> nodes, Object context) {
        if (CollectionUtilities.isEmpty(nodes)) {
            return null;
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        return nodes.get(index.getAndIncrement() % nodes.size());
    }

}
