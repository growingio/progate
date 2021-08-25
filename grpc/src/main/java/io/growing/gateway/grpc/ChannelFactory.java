package io.growing.gateway.grpc;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.meta.Upstream;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.time.Duration;

public final class ChannelFactory {

    private static final LoadingCache<ServerNode, ManagedChannel> CHANNELS = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofMinutes(5)).removalListener((key, value, cause) -> {
            final ManagedChannel channel = (ManagedChannel) value;
            channel.shutdown();
        }).build(new CacheLoader<>() {
            @Override
            public @Nullable
            ManagedChannel load(ServerNode key) throws Exception {
                return ManagedChannelBuilder.forAddress(key.host(), key.port()).usePlaintext().build();
            }
        });

    private ChannelFactory() {
    }

    public static ManagedChannel get(final ServerNode node) {
        return CHANNELS.get(node);
    }

    public static ManagedChannel get(final Upstream upstream, final Object context) {
        final ServerNode node = upstream.balancer().select(upstream.getAvailableNodes(), context);
        return get(node);
    }

}
