package io.growing.gateway.ctrl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.growing.gateway.meta.ServerNode;
import io.vertx.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

public abstract class AbstractScheduledHealthService implements HealthService {
    private final Vertx vertx;
    private final Function<ServerNode, HealthStatus> checker = createChecker();
    private final Cache<ServerNode, Long> timers = Caffeine.newBuilder().build();
    private final Logger logger = LoggerFactory.getLogger("health-service");
    private final Cache<ServerNode, HealthStatus> statusCache = Caffeine.newBuilder().expireAfterWrite(Duration.ofMinutes(1)).build();

    protected AbstractScheduledHealthService(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public HealthStatus check(ServerNode node) {
        final HealthStatus status = statusCache.getIfPresent(node);
        if (Objects.isNull(status)) {
            return HealthStatus.UNKNOWN;
        }
        return status;
    }

    @Override
    public void unwatch(ServerNode node) {
        final Long id = timers.getIfPresent(node);
        if (Objects.nonNull(id)) {
            vertx.cancelTimer(id);
            timers.invalidate(node);
        }
    }

    @Override
    public void watch(ServerNode node, HealthCheck check) {
        doCheck(node);
        timers.get(node, timerKey -> vertx.setPeriodic(check.interval(), id -> {
            doCheck(node);
        }));
    }

    private void doCheck(ServerNode node) {
        final String host = node.host();
        final int port = node.port();
        logger.info("Health check host: {}, port: {}", host, port);
        final HealthStatus status = checker.apply(node);
        logger.info("Health check host: {}, port: {}, status: {}", host, port, status);
        statusCache.put(node, status);
    }

    protected abstract Function<ServerNode, HealthStatus> createChecker();

}
