package io.growing.gateway.restful.idl;

import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;

import java.util.List;
import java.util.Set;

/**
 * @Description: restful Api 请求的基本配置
 * @Author: zhuhongbin
 * @Date 2021/9/22 1:55 下午
 **/
public class RestfulBuilder {

    private Set<Outgoing> outgoings;
    private PluginFetcherBuilder pfb;
    private List<ServiceMetadata> services;
    private RestfulExceptionHandler exceptionHandler;

    public static RestfulBuilder newBuilder() {
        return new RestfulBuilder();
    }

    public RestfulBuilder services(final List<ServiceMetadata> services) {
        this.services = services;
        return this;
    }

    public RestfulBuilder outgoings(final Set<Outgoing> outgoings) {
        this.outgoings = outgoings;
        return this;
    }

    public RestfulBuilder configFactory(final ConfigFactory configFactory) {
        this.pfb = new PluginFetcherBuilder(configFactory);
        return this;
    }

    public RestfulBuilder exceptionHandler(final RestfulExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        return this;
    }

    public RestfulApi build() {
        return new RestfulApi(outgoings, pfb, services, exceptionHandler);
    }
}