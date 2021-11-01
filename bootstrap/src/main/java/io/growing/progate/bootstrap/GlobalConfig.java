package io.growing.progate.bootstrap;

import io.growing.gateway.config.OAuth2Config;
import io.growing.gateway.graphql.config.GraphqlConfig;
import io.growing.gateway.restful.config.RestfulConfig;
import io.growing.progate.bootstrap.config.ServerConfig;

public class GlobalConfig {
    private ServerConfig server;
    private HashIdConfig hashids;
    private GraphqlConfig graphql;
    private RestfulConfig restful;
    private OAuth2Config oauth2;

    public static class HashIdConfig {
        private String salt;
        private int length;

        public String getSalt() {
            return salt;
        }

        public void setSalt(String salt) {
            this.salt = salt;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }
    }

    public ServerConfig getServer() {
        return server;
    }

    public void setServer(ServerConfig server) {
        this.server = server;
    }

    public HashIdConfig getHashids() {
        return hashids;
    }

    public void setHashids(HashIdConfig hashids) {
        this.hashids = hashids;
    }

    public GraphqlConfig getGraphql() {
        return graphql;
    }

    public void setGraphql(GraphqlConfig graphql) {
        this.graphql = graphql;
    }

    public RestfulConfig getRestful() {
        return restful;
    }

    public void setRestful(RestfulConfig restful) {
        this.restful = restful;
    }

    public OAuth2Config getOauth2() {
        return oauth2;
    }

    public void setOauth2(OAuth2Config oauth2) {
        this.oauth2 = oauth2;
    }
}
