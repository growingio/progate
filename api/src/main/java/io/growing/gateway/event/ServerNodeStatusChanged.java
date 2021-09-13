package io.growing.gateway.event;

import io.growing.gateway.ctrl.HealthStatus;
import io.growing.gateway.meta.ServerNode;
import io.vertx.core.json.JsonObject;

public class ServerNodeStatusChanged {
    public static final String TOPIC = "server.status.changed";
    private final ServerNode node;
    private final HealthStatus originStatus;
    private final HealthStatus currentStatus;

    public ServerNodeStatusChanged(ServerNode node, HealthStatus originStatus, HealthStatus currentStatus) {
        this.node = node;
        this.originStatus = originStatus;
        this.currentStatus = currentStatus;
    }

    public ServerNode getNode() {
        return node;
    }

    public HealthStatus getOriginStatus() {
        return originStatus;
    }

    public HealthStatus getCurrentStatus() {
        return currentStatus;
    }

    public JsonObject toJson() {
        final JsonObject json = new JsonObject();
        json.put("originStatus", originStatus);
        json.put("currentStatus", currentStatus);
        return json;
    }

}
