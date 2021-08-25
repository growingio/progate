package io.growing.gateway.cluster;

import io.growing.gateway.meta.ServerNode;

import java.util.List;

public interface LoadBalance {

    ServerNode select(List<ServerNode> nodes, Object context);

}
