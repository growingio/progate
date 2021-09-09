package io.growing.gateway.ctrl;

import io.growing.gateway.meta.ServerNode;

public interface HealthService {

    void unwatch(ServerNode node);

    HealthStatus check(ServerNode node);

    void watch(ServerNode node, HealthCheck check);

}
