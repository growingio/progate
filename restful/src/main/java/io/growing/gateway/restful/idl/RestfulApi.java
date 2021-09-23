package io.growing.gateway.restful.idl;

import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.vertx.core.http.HttpServerRequest;

import java.util.List;
import java.util.Set;

/**
 * @Description: RestApi 定义
 * @Author: zhuhongbin
 * @Date 2021/9/22 1:58 下午
 **/
public class RestfulApi {
    private List<ServiceMetadata> services;
    private Set<Outgoing> outgoings;
    private HttpServerRequest request;
    private RestfulExceptionHandler exceptionHandler;
    private PluginFetcherBuilder pfb;

    public RestfulApi(List<ServiceMetadata> services, Set<Outgoing> outgoings, HttpServerRequest request) {
        this.services = services;
        this.outgoings = outgoings;
        this.request = request;
    }

    public RestfulApi(Set<Outgoing> outgoings, PluginFetcherBuilder pfb, List<ServiceMetadata> services, RestfulExceptionHandler exceptionHandler) {
    }

    // 执行所有的方法
    public void execute(HttpServerRequest request) {

    }
}
