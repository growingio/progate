package io.growing.gateway.http;

import io.vertx.core.http.HttpMethod;

import java.util.Set;

/**
 * @author AI
 */
public class HttpApi {
    private String upstreamName;
    private String path;
    private String grpcService;
    private Set<HttpMethod> methods;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Set<HttpMethod> getMethods() {
        return methods;
    }

    public void setMethods(Set<HttpMethod> methods) {
        this.methods = methods;
    }

    public String getGrpcService() {
        return grpcService;
    }

    public void setGrpcService(String grpcService) {
        this.grpcService = grpcService;
    }

    public String getUpstreamName() {
        return upstreamName;
    }

    public void setUpstreamName(String upstreamName) {
        this.upstreamName = upstreamName;
    }
}
