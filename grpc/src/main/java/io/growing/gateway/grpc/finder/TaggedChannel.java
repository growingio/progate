package io.growing.gateway.grpc.finder;

import io.growing.gateway.meta.ServerNode;
import io.grpc.Channel;

public class TaggedChannel {
    private final Channel channel;
    private final ServerNode node;

    public TaggedChannel(Channel channel, ServerNode node) {
        this.channel = channel;
        this.node = node;
    }

    public static TaggedChannel from(final Channel channel, final ServerNode node) {
        return new TaggedChannel(channel, node);
    }

    public Channel getChannel() {
        return channel;
    }

    public ServerNode getNode() {
        return node;
    }

}
