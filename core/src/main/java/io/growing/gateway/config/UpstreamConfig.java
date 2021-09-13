package io.growing.gateway.config;

import io.growing.gateway.cluster.LoadBalance;
import io.growing.gateway.cluster.RoundRobin;
import io.growing.gateway.ctrl.HealthCheck;
import io.growing.gateway.ctrl.HealthService;
import io.growing.gateway.ctrl.HealthStatus;
import io.growing.gateway.meta.ServerNode;
import io.growing.gateway.meta.Upstream;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UpstreamConfig {

    private String name;
    private String protocol;
    private List<Node> nodes;
    private String balancer;
    private boolean internal = false;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public String getBalancer() {
        return balancer;
    }

    public void setBalancer(String balancer) {
        this.balancer = balancer;
    }

    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    public static class Node {
        private String host;
        private int port;
        private int weight;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        ServerNode toServerNode(final HealthService healthService) {
            final ServerNode node = new ServerNode() {
                @Override
                public String id() {
                    return host + ":" + port;
                }

                @Override
                public String host() {
                    return host;
                }

                @Override
                public int port() {
                    return port;
                }

                @Override
                public int weight() {
                    return weight;
                }

                @Override
                public boolean isAvailable() {
                    return HealthStatus.HEALTHY == healthService.check(this);
                }
            };
            healthService.watch(node, () -> TimeUnit.SECONDS.toMillis(1));
            return node;
        }
    }

    public Upstream toUpstream(final HealthService healthService) {
        final List<ServerNode> servers = new LinkedList<>();
        nodes.forEach(node -> servers.add(node.toServerNode(healthService)));
        return new Upstream() {
            @Override
            public boolean isInternal() {
                return internal;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public String protocol() {
                return protocol;
            }

            @Override
            public List<ServerNode> nodes() {
                return servers;
            }

            @Override
            public LoadBalance balancer() {
                return new RoundRobin();
            }
        };
    }

}
