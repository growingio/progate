package io.growing.progate.bootstrap.config;

import io.growing.gateway.graphql.config.GraphqlConfig;

public class InboundConfig {
    private GraphqlConfig graphql;

    public GraphqlConfig getGraphql() {
        return graphql;
    }

    public void setGraphql(GraphqlConfig graphql) {
        this.graphql = graphql;
    }
}
