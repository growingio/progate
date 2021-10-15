package io.growing.gateway.restful;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.grpc.json.Jackson;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.lang.HashIdCodec;
import io.growing.gateway.restful.config.RestfulConfig;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.idl.RestfulApi;
import io.growing.gateway.restful.idl.RestfulBuilder;
import io.growing.gateway.restful.idl.RestfulHttpApi;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/***
 * @date: 2021/9/18 5:38 下午
 * @description: restful
 * @author: zhuhongbin
 **/
public class RestfulIncoming implements Incoming {

    private final Logger logger = LoggerFactory.getLogger(RestfulIncoming.class);
    private final AtomicReference<Set<RestfulApi>> restfulApiAtomicReference = new AtomicReference<>();

    private final RestfulExceptionHandler restfulExceptionHandler = new RestfulExceptionHandler();
    private final Gson gson;
    private final RestfulConfig config;
    private final HashIdCodec hashIdCodec;
    private final ConfigFactory configFactory;

    public RestfulIncoming(RestfulConfig config, HashIdCodec hashIdCodec, ConfigFactory configFactory) {
        this.config = config;
        this.hashIdCodec = hashIdCodec;
        this.configFactory = configFactory;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    public void reload(final List<ServiceMetadata> services, final Set<Outgoing> outgoings) {
        // reload 加载接口的定义和映射
        RestfulBuilder restfulBuilder = RestfulBuilder.newBuilder();
        restfulApiAtomicReference.set(restfulBuilder.outgoings(outgoings).services(services).exceptionHandler(restfulExceptionHandler).build());
    }

    @Override
    public Set<HttpApi> apis() {
        // 初始化所有路由（不会使用）
        Set<HttpApi> httpApis = Sets.newHashSet();
        final HttpApi httpApi = new HttpApi();
        final HashSet<HttpMethod> httpMethods = Sets.newHashSet();
        HttpMethod.values().forEach(httpMethod -> httpMethods.add(httpMethod));
        httpApi.setMethods(httpMethods);
        new HttpApi().setPath(config.getPath() + "/**");
        return httpApis;
    }

    @Override
    public Set<HttpApi> apis(List<ServiceMetadata> services) {
        // 初始化所有路由
        final String basePath = config.getPath();
        Set<HttpApi> httpApis = Sets.newHashSet();
        if (Objects.nonNull(services)) {
            services.forEach(serviceMetadata -> {
                serviceMetadata.restfulDefinitions().forEach(endpointDefinition -> {
                    try {
                        OpenAPI openAPI = Jackson.YAMLMAPPPER.readValue(endpointDefinition.getContent(), OpenAPI.class);
                        for (Map.Entry<String, PathItem> path : openAPI.getPaths().entrySet()) {
                            final PathItem pathItem = path.getValue();
                            if (Objects.nonNull(pathItem)) {
                                final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
                                operationMap.forEach((httpMethod, operation) -> {
                                    final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
                                    if (Objects.isNull(endpoint)) {
                                        throw new RuntimeException("x-grpc-endpoint must defined in you proto file");
                                    }
                                    RestfulHttpApi restfulHttpApi = new RestfulHttpApi();
                                    Set<HttpMethod> methods = Sets.newHashSet();
                                    final String pathKey = path.getKey().replace(RestfulConstants.REST_PATH_KEY, RestfulConstants.VERTX_PATH_KEY);
                                    restfulHttpApi.setPath(basePath + pathKey);
                                    methods.add(new HttpMethod(httpMethod.name()));
                                    restfulHttpApi.setMethods(methods);
                                    restfulHttpApi.setGrpcDefination(endpoint.toString());
                                    restfulHttpApi.setApiResponses(operation.getResponses());
                                    httpApis.add(restfulHttpApi);
                                });
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("openapi yaml 格式定义错误，当前服务是：{}", serviceMetadata.upstream());
                    }
                });
            });
        }
        return httpApis;
    }

    @Override
    public void handle(HttpServerRequest request) {
    }

    @Override
    public void handle(HttpApi httpApi, HttpServerRequest request) {
        if (Objects.isNull(request) || Objects.isNull(httpApi)) {
            throw new RuntimeException(" 不合法的请求");
        }
        logger.info("restful 请求入口，Restful请求：{},请求头信息：{}", httpApi, request.getHeader("Authorize"));
        request.bodyHandler(handle -> {
            final JsonObject jsonObject = handle.toJsonObject();
            Map<String, Object> params = new HashMap<>();
            if (Objects.nonNull(jsonObject)) {
                params = Json.decodeValue(jsonObject.toString(), Map.class);
            }
            Map<String, Object> finalParams = params;
            request.params().forEach(param -> {
                finalParams.put(param.getKey(), param.getValue());
            });
            if (httpApi instanceof RestfulHttpApi) {
                final RestfulHttpApi restfulHttpApi = (RestfulHttpApi) httpApi;
                final String projectId = request.getParam(RestfulConstants.PROJECT_KEY);
                if (StringUtils.isNotBlank(projectId)) {
                    finalParams.put(RestfulConstants.PROJECT_KEY, hashIdCodec.decode(projectId));
                }
                Optional<RestfulApi> restfulApi = restfulApiAtomicReference.get().stream().filter(api -> {
                    return api.getGrpcDefination().equalsIgnoreCase(restfulHttpApi.getGrpcDefination());
                }).collect(Collectors.toList()).stream().findFirst();
                if (restfulApi.isPresent()) {
                    final CompletableFuture<Object> completableFuture = restfulApi.get().execute(config.getPath(), restfulHttpApi, finalParams);
                    completableFuture.whenComplete((result, t) -> {
                        HttpServerResponse response = request.response();
                        response.headers().set(HttpHeaders.CONTENT_TYPE, RestfulConstants.CONTENT_TYPE);
                        response.end(gson.toJson(result));
                    });
                } else {
                    logger.warn("restful 当前请求路径尚未开放: {}", httpApi.getPath());
                    throw new RuntimeException("当前请求路径尚未开放");
                }
            } else {
                logger.error("当前请求不是restful 请求，请校验请求路径");
                throw new RuntimeException("当前请求不是restful 请求，请校验请求路径");
            }
        });
    }
}
