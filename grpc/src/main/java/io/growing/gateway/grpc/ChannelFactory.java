package io.growing.gateway.grpc;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.growing.gateway.grpc.finder.TaggedChannel;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.meta.Upstream;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

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

    public static TaggedChannel get(final ServerNode node) {
        final ManagedChannel channel = CHANNELS.get(node);
        assert Objects.nonNull(channel);
        if (channel.isShutdown() || channel.isTerminated()) {
            CHANNELS.invalidate(node);
            return TaggedChannel.from(CHANNELS.get(node), node);
        }
        return TaggedChannel.from(channel, node);
    }

    public static TaggedChannel get(final Upstream upstream, final Object context) {
        final ServerNode node = upstream.balancer().select(upstream.getAvailableNodes(), context);
        return get(node);
    }

}
