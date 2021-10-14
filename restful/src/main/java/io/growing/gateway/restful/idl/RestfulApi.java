package io.growing.gateway.restful.idl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.api.RestfulRequestContext;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.utils.RestfulResult;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
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

    public CompletableFuture<Object> execute(String path, RestfulHttpApi httpApi, Map<String, Object> params) {
        RequestContext requestContext = new RestfulRequestContext(params);
        final CompletableFuture<?> completionStage = (CompletableFuture<?>) outgoing.handle(serviceMetadata.upstream(), grpcDefination, requestContext);
        return completionStage.thenApply(result -> {
            return wrap(result, httpApi.getApiResponses().getDefault());
        });

    }

    /***
     * @date: 2021/10/8 11:17 上午
     * @description: 结果包装
     * @author: zhuhongbin
     **/
    private Object wrap(final Object result, ApiResponse apiResponse) {
        final Object res = ((Collection) result).iterator().next();
        if (result instanceof Collection) {
            if (res instanceof DynamicMessageWrapper) {
                final DynamicMessageWrapper messageWrapper = ((DynamicMessageWrapper) res);
                final Object definationSchema = apiResponse.getContent().get("application/json").getSchema().getProperties().get("data");
                final Schema schema = Json.decodeValue(Json.encode(definationSchema), Schema.class);
                final Map<String, Object> properties = schema.getProperties();
                Map<String, Object> resultData = new HashMap<>();
                properties.keySet().forEach(key -> {
                    resultData.put(key, messageWrapper.get(key));
                });
                RestfulResult restfulResult = new RestfulResult();
                restfulResult.setCode("200");
                restfulResult.setData(resultData);
                restfulResult.setElasped(10000);
                restfulResult.setError("");
                return restfulResult;
            } else {
                RestfulResult restfulResult = new RestfulResult();
                restfulResult.setCode("400");
                restfulResult.setData(null);
                restfulResult.setElasped(10000);
                restfulResult.setError("失败");
                return restfulResult;
            }
        } else {
            RestfulResult restfulResult = new RestfulResult();
            restfulResult.setCode("400");
            restfulResult.setData(null);
            restfulResult.setElasped(10000);
            restfulResult.setError("失败");
            return restfulResult;
        }
    }
}
