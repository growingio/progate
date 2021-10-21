package io.growing.gateway.restful;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.config.OAuth2Config;
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
import io.growing.gateway.restful.utils.RestfulResult;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
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
    private final WebClient webClient;
    private final HashIdCodec hashIdCodec;
    private final OAuth2Config oAuth2Config;

    public RestfulIncoming(RestfulConfig config, HashIdCodec hashIdCodec, WebClient webClient, OAuth2Config oAuth2Config) {
        this.config = config;
        this.hashIdCodec = hashIdCodec;
        this.gson = new GsonBuilder().serializeNulls().create();
        this.webClient = webClient;
        this.oAuth2Config = oAuth2Config;
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
        httpMethods.addAll(HttpMethod.values());
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
                    final OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
                    final SwaggerParseResult swaggerParseResult = openAPIV3Parser.readContents(new String(endpointDefinition.getContent(), StandardCharsets.UTF_8));
                    final OpenAPI openAPI = swaggerParseResult.getOpenAPI();
                    final Paths paths = openAPI.getPaths();
                    if (Objects.nonNull(paths) && !paths.isEmpty()) {
                        paths.forEach((key, pathItem) -> {
                            final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
                            operationMap.forEach((httpMethod, operation) -> {

                                // final Schema schema = operation.getRequestBody().getContent().get(RestfulConstants.OPENAPI_MEDIA_TYPE).getSchema();
                                final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
                                if (Objects.isNull(endpoint)) {
                                    throw new RuntimeException("x-grpc-endpoint must defined in you proto file");
                                }
                                RestfulHttpApi restfulHttpApi = new RestfulHttpApi();
                                Set<HttpMethod> methods = Sets.newHashSet();
                                final String pathKey = key.replace(RestfulConstants.REST_PATH_KEY, RestfulConstants.VERTX_PATH_KEY);
                                restfulHttpApi.setPath(basePath + pathKey);
                                methods.add(new HttpMethod(httpMethod.name()));
                                restfulHttpApi.setMethods(methods);
                                restfulHttpApi.setGrpcDefinition(endpoint.toString());
                                restfulHttpApi.setApiResponses(operation.getResponses());
                                httpApis.add(restfulHttpApi);
                            });
                        });
                    }
                });
            });
        }
        return httpApis;
    }

    @Override
    public void handle(HttpServerRequest request) {
        // donoting
    }

    @Override
    public void handle(HttpApi httpApi, HttpServerRequest request) {
        final HttpServerResponse response = request.response();
        if (Objects.isNull(request) || Objects.isNull(httpApi)) {
            response.end(gson.toJson(RestfulResult.error("不合法的请求")));
        }
        request.pause();
        final String oauthToken = request.getHeader(RestfulConstants.AUTHORIZE);
        logger.info("restful 请求入口，Restful请求：{},请求头信息：{}", httpApi, oauthToken);
        if (StringUtils.isBlank(oauthToken)) {
            response.end(gson.toJson(RestfulResult.error("token 认证失败")));
        }
        // Token 校验
        webClient.get(oAuth2Config.getAuthServer(), oAuth2Config.getTokenCheckUrl())
            .bearerTokenAuthentication(oauthToken)
            .send()
            .onSuccess(handler -> {
                logger.info("token 认证通过。:{}", handler);
                request.resume();
                final JsonObject jsonObject = handler.bodyAsJsonObject();
                final String clientId = jsonObject.getString("client_id");
                doHandle(httpApi, request, clientId);
            })
            .onFailure(error -> {
                logger.error("token 认证失败", error);
                response.end(gson.toJson(RestfulResult.error("token 认证失败")));
            });

    }

    /***
     * @date: 2021/10/18 1:23 下午
     * @description: 解析执行
     * @author: zhuhongbin
     **/
    private void doHandle(HttpApi httpApi, HttpServerRequest request, String clientId) {
        request.bodyHandler(handle -> {
            // 响应 response（全局）
            HttpServerResponse response = request.response();
            response.headers().set(HttpHeaders.CONTENT_TYPE, RestfulConstants.CONTENT_TYPE);
            // 请求 request
            Map<String, Object> params = new HashMap<>();
            if (Objects.nonNull(handle)) {
                params = Json.decodeValue(handle, Map.class);
            }
            Map<String, Object> finalParams = params;
            request.params().forEach(param -> finalParams.put(param.getKey(), param.getValue()));
            finalParams.put(RestfulConstants.X_REQUEST_ID, request.getHeader(RestfulConstants.X_REQUEST_ID));

            if (httpApi instanceof RestfulHttpApi) {
                final RestfulHttpApi restfulHttpApi = (RestfulHttpApi) httpApi;
                final String projectId = request.getParam(RestfulConstants.PROJECT_KEY);
                if (StringUtils.isNotBlank(projectId)) {
                    finalParams.put(RestfulConstants.PROJECT_KEY, hashIdCodec.decode(projectId));
                }
                Optional<RestfulApi> restfulApi = restfulApiAtomicReference.get().stream().filter(api -> {
                    return api.getGrpcDefination().equalsIgnoreCase(restfulHttpApi.getGrpcDefinition());
                }).collect(Collectors.toList()).stream().findFirst();

                if (restfulApi.isPresent()) {
                    final CompletableFuture<Object> completableFuture = restfulApi.get().execute(config.getPath(), restfulHttpApi, finalParams);
                    completableFuture.whenComplete((result, throwable) -> {
                        if (Objects.nonNull(throwable)) {
                            logger.warn("请求异常: {}", httpApi.getPath());
                            response.end(gson.toJson(RestfulResult.error(throwable.getMessage())));
                        }
                        response.end(gson.toJson(result));
                    });
                } else {
                    logger.warn("restful 当前请求路径尚未开放: {}", httpApi.getPath());
                    response.end(gson.toJson(RestfulResult.error("restful 当前请求路径尚未开放")));
                }
            } else {
                logger.error("当前请求不是restful 请求，请校验请求路径");
                response.end(gson.toJson(RestfulResult.error("当前请求不是restful 请求，请校验请求路径")));
            }
        });
    }
}
