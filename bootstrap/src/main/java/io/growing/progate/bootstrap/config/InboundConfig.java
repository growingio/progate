package io.growing.progate.bootstrap.config;

import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.progate.restful.config.RestfulConfig;

public class InboundConfig {

    private GraphqlConfig graphql;
    private RestfulConfig restful;

    @Component
    public GraphqlConfig getGraphql() {
        return graphql;
    }

    public void setGraphql(GraphqlConfig graphql) {
        this.graphql = graphql;
    }

    @Component
    public RestfulConfig getRestful() {
        return restful;
    }

    public void setRestful(RestfulConfig restful) {
        this.restful = restful;
    }

}
