package io.growing.gateway.restful.idl;

import io.growing.gateway.context.RequestContext;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.restful.api.RestfulRequestContext;
import io.growing.gateway.restful.enums.DataTypeFormat;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.growing.gateway.restful.utils.RestfulResult;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class RestfulApi {
    private ServiceMetadata serviceMetadata;
    private String grpcDefinition;
    private Outgoing outgoing;

    public RestfulApi() {
    }

    public RestfulApi(ServiceMetadata serviceMetadata, String grpcDefinition, Outgoing outgoing) {
        this.serviceMetadata = serviceMetadata;
        this.grpcDefinition = grpcDefinition;
        this.outgoing = outgoing;
    }


    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public String getGrpcDefinition() {
        return grpcDefinition;
    }

    public void setGrpcDefinition(String grpcDefinition) {
        this.grpcDefinition = grpcDefinition;
    }

    public Outgoing getOutgoing() {
        return outgoing;
    }

    public void setOutgoing(Outgoing outgoing) {
        this.outgoing = outgoing;
    }

    public CompletableFuture<Object> execute(final String path, final RestfulHttpApi httpApi, final Map<String, Object> params) {
        RequestContext requestContext = new RestfulRequestContext(params);
        final long start = System.currentTimeMillis();
        final CompletableFuture<?> completionStage = (CompletableFuture<?>) outgoing.handle(serviceMetadata.upstream(), grpcDefinition, requestContext);
        return completionStage.thenApply(result -> {
            final RestfulResult restfulResult = wrap(result, httpApi.getApiResponses().getDefault());
            final long end = System.currentTimeMillis();
            restfulResult.setElasped(end - start);
            return restfulResult;
        });

    }

    private RestfulResult wrap(final Object result, final ApiResponse apiResponse) {
        if (Objects.isNull(result)) {
            return RestfulResult.success("");
        }
        final Collection results = (Collection) result;
        final Schema schema = (Schema) apiResponse.getContent().get(RestfulConstants.OPENAPI_MEDIA_TYPE).getSchema().getProperties().get(RestfulConstants.RESULT_DATA);
        final Map<String, Schema> properties = schema.getProperties();
        if (results.size() == 1) {
            Object res = results.iterator().next();
            if (res instanceof DynamicMessageWrapper) {
                final DynamicMessageWrapper messageWrapper = ((DynamicMessageWrapper) res);
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

    private Map<String, Object> resultWrap(DynamicMessageWrapper messageWrapper, Map<String, Schema> properties ) {
        Map<String, Object> resultData = new HashMap<>(messageWrapper.values().size());
        properties.keySet().forEach(key -> {
            final Schema schema = properties.get(key);
            if (schema instanceof StringSchema) {
                StringSchema stringSchema = (StringSchema) schema;
                final String format = StringUtils.isBlank(stringSchema.getFormat()) ? "" : stringSchema.getFormat();
                if (Objects.nonNull(messageWrapper.get(key)) && StringUtils.isNotBlank(format)) {
                    if (DataTypeFormat.HASHID.getName().equalsIgnoreCase(format)) {
                        resultData.put(key,"//TODO" );//hashIdCodec.encode(Long.valueOf(messageWrapper.get(key).toString())));
                    } else {
                        resultData.put(key, messageWrapper.get(key));
                    }
                }
            } else if (schema instanceof ArraySchema) {
                ArraySchema arraySchema = (ArraySchema) schema;
                final String format = StringUtils.isBlank(arraySchema.getItems().getFormat()) ? "" : arraySchema.getItems().getFormat();
                if (DataTypeFormat.HASHID.getName().equalsIgnoreCase(format)) {
                    // process gRPC api results
                    String[] encodes = new String[]{};
                    final String[] results = messageWrapper.get(key).toString().split(",");
                    for (int i = 0; i < results.length; i++) {
                        encodes[i] = "//TODO";  //hashIdCodec.encode(Long.parseLong(results[i]));
                    }
                    resultData.put(key, encodes);
                } else {
                    resultData.put(key, messageWrapper.get(key));
                }
            } else {
                resultData.put(key, messageWrapper.get(key));
            }
        });
        return resultData;
    }

}
