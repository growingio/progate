package io.growing.gateway.grpc.ctrl;

import io.growing.gateway.ctrl.AbstractScheduledHealthService;
import io.growing.gateway.ctrl.HealthStatus;
import io.growing.gateway.grpc.ChannelFactory;
import io.growing.gateway.meta.ServerNode;
import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.vertx.core.Vertx;

import javax.inject.Inject;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class GrpcHealthService extends AbstractScheduledHealthService {

    @Inject
    public GrpcHealthService(Vertx vertx) {
        super(vertx);
    }

    @Override
    protected Function<ServerNode, HealthStatus> createChecker() {
        return node -> {
            final ManagedChannel channel = ChannelFactory.get(node);
            final HealthGrpc.HealthBlockingStub stub = HealthGrpc.newBlockingStub(channel).withDeadline(Deadline.after(1, TimeUnit.MINUTES));
            HealthStatus status;
            try {
                final HealthCheckRequest request = HealthCheckRequest.getDefaultInstance();
                final HealthCheckResponse response = stub.check(request);
                switch (response.getStatus()) {
                    case SERVING:
                        status = HealthStatus.HEALTHY;
                        break;
                    case NOT_SERVING:
                        status = HealthStatus.UNHEALTHY;
                        break;
                    default:
                        status = HealthStatus.UNKNOWN;
                        break;
                }
            } catch (StatusRuntimeException e) {
                if (e.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED) {
                    status = HealthStatus.TIMEOUT;
                } else {
                    status = HealthStatus.UNHEALTHY;
                }
            }
            return status;
        };
    }
}
