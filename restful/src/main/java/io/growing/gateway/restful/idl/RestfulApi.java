package io.growing.gateway.restful.idl;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.api.RestfulRequestContext;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.vertx.core.http.HttpServerRequest;

import java.util.Map;

/**
 * @Description: RestApi 定义
 * @Author: zhuhongbin
 * @Date 2021/9/22 1:58 下午
 **/
public class RestfulApi {
    private ServiceMetadata serviceMetadata;
    private String grpcService;
    private Outgoing outgoing;
    private RestfulExceptionHandler exceptionHandler;
    private PluginFetcherBuilder pfb;

    public RestfulApi() {
    }

    public RestfulApi(ServiceMetadata serviceMetadata, String grpcService, Outgoing outgoing, RestfulExceptionHandler exceptionHandler, PluginFetcherBuilder pfb) {
        this.serviceMetadata = serviceMetadata;
        this.grpcService = grpcService;
        this.outgoing = outgoing;
        this.exceptionHandler = exceptionHandler;
        this.pfb = pfb;
    }


    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public String getGrpcService() {
        return grpcService;
    }

    public void setGrpcService(String grpcService) {
        this.grpcService = grpcService;
    }

    public RestfulExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(RestfulExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public PluginFetcherBuilder getPfb() {
        return pfb;
    }

    public void setPfb(PluginFetcherBuilder pfb) {
        this.pfb = pfb;
    }

    public Outgoing getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Outgoing outgoing) {
        this.outgoing = outgoing;
    }

    public void execute(String path, HttpApi httpApi, HttpServerRequest request) {
        final String upstreamName = httpApi.getUpstreamName();
        RequestContext requestContext = new RestfulRequestContext((Map<String, Object>) request.params());
        outgoing.handle(serviceMetadata.upstream(), grpcService, null);
    }

}
