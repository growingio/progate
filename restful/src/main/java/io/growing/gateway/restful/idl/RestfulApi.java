package io.growing.gateway.restful.idl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.api.RestfulRequestContext;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.utils.RestfulResult;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: RestApi 定义
 * @Author: zhuhongbin
 * @Date 2021/9/22 1:58 下午
 **/
public class RestfulApi {
    private ServiceMetadata serviceMetadata;
    private String grpcDefination;
    private Outgoing outgoing;
    private RestfulExceptionHandler exceptionHandler;
    private PluginFetcherBuilder pfb;
    private Gson gson;

    public RestfulApi() {
    }

    public RestfulApi(ServiceMetadata serviceMetadata, String grpcDefination, Outgoing outgoing, RestfulExceptionHandler exceptionHandler, PluginFetcherBuilder pfb) {
        this.serviceMetadata = serviceMetadata;
        this.grpcDefination = grpcDefination;
        this.outgoing = outgoing;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.exceptionHandler = exceptionHandler;
        this.pfb = pfb;
    }


    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public String getGrpcDefination() {
        return grpcDefination;
    }

    public void setGrpcDefination(String grpcDefination) {
        this.grpcDefination = grpcDefination;
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

    public CompletableFuture<RestfulResult> execute(String path, HttpApi httpApi, HttpServerRequest request) {
        Map<String, Object> params = new HashMap<>(request.params().size());
        final MultiMap requestParams = request.params();
        requestParams.forEach(param -> {
            params.put(param.getKey(), param.getValue());
        });
        params.put("id", "1");
        RequestContext requestContext = new RestfulRequestContext(params);
        final CompletableFuture<?> completionStage = (CompletableFuture<?>) outgoing.handle(serviceMetadata.upstream(), grpcDefination, requestContext);
        return completionStage.thenApply(result -> {
            if (result instanceof Collection) {
                final Object res = ((Collection) result).iterator().next();
                if (res instanceof DynamicMessageWrapper) {
                    final Collection<Object> values = ((DynamicMessageWrapper) res).values();
                    RestfulResult restfulResult = new RestfulResult();
                    restfulResult.add(Json.encode(values));
                    return restfulResult;
                }
            }
            return null;
        });
    }
}
