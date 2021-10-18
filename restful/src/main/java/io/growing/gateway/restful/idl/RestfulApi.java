package io.growing.gateway.restful.idl;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.api.RestfulRequestContext;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.growing.gateway.restful.utils.RestfulResult;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @Description: RestApi 定义
 * @Author: zhuhongbin
 * @Date 2021/9/22 1:58 下午
 **/
public class RestfulApi {
    private final Logger logger = LoggerFactory.getLogger(RestfulApi.class);
    private ServiceMetadata serviceMetadata;
    private String grpcDefination;
    private Outgoing outgoing;
    private RestfulExceptionHandler exceptionHandler;
    private PluginFetcherBuilder pfb;

    public RestfulApi() {
    }

    public RestfulApi(ServiceMetadata serviceMetadata, String grpcDefination, Outgoing outgoing, RestfulExceptionHandler exceptionHandler, PluginFetcherBuilder pfb) {
        this.serviceMetadata = serviceMetadata;
        this.grpcDefination = grpcDefination;
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
        final long start = System.currentTimeMillis();
        final CompletableFuture<?> completionStage = (CompletableFuture<?>) outgoing.handle(serviceMetadata.upstream(), grpcDefination, requestContext);
        return completionStage.thenApply(result -> {
            final RestfulResult restfulResult = wrap(result, httpApi.getApiResponses().getDefault());
            final long end = System.currentTimeMillis();
            restfulResult.setElasped(end - start);
            return restfulResult;
        });

    }

    /***
     * @date: 2021/10/8 11:17 上午
     * @description: 结果包装
     * @author: zhuhongbin
     **/
    private RestfulResult wrap(final Object result, ApiResponse apiResponse) {
        if (Objects.isNull(result)) {
            return RestfulResult.success(null);
        }
        final Collection results = (Collection) result;
        final Object definationSchema = apiResponse.getContent().get(RestfulConstants.OPENAPI_MEDIA_TYPE).getSchema().getProperties().get(RestfulConstants.RESULT_DATA);
        final Schema schema = Json.decodeValue(Json.encode(definationSchema), Schema.class);
        final Map<String, Object> properties = schema.getProperties();
        if (results.size() == 1) {
            Object res = results.iterator().next();
            final DynamicMessageWrapper messageWrapper = ((DynamicMessageWrapper) res);
            if (res instanceof DynamicMessageWrapper) {
                final Map<String, Object> resultWrap = resultWrap(messageWrapper, properties);
                return RestfulResult.success(resultWrap);
            }
            return RestfulResult.success(res);
        } else {
            final List<DynamicMessageWrapper> messageWrappers = (List<DynamicMessageWrapper>) result;
            List<Map<String, Object>> res = new ArrayList<>();
            messageWrappers.forEach(dynamicMessageWrapper -> {
                res.add(resultWrap(dynamicMessageWrapper, properties));
            });
            return RestfulResult.success(res);
        }
    }

    /***
     * @date: 2021/10/18 6:51 下午
     * @description: 结果包装
     * @author: zhuhongbin
     **/
    private Map<String, Object> resultWrap(DynamicMessageWrapper messageWrapper, Map<String, Object> properties) {
        Map<String, Object> resultData = new HashMap<>();
        properties.keySet().forEach(key -> {
            resultData.put(key, messageWrapper.get(key));
        });
        return resultData;
    }
}
