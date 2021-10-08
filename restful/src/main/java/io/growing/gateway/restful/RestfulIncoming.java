package io.growing.gateway.restful;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.restful.config.RestfulConfig;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.idl.RestfulApi;
import io.growing.gateway.restful.idl.RestfulBuilder;
import io.growing.gateway.restful.idl.RestfulHttpApi;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.Json;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
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
    private final ConfigFactory configFactory;

    public RestfulIncoming(RestfulConfig config, ConfigFactory configFactory) {
        this.config = config;
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
        return null;
    }

    @Override
    public Set<HttpApi> apis(List<ServiceMetadata> services) {
        // 初始化所有路由
        final String basePath = config.getPath();
        Set<HttpApi> httpApis = Sets.newHashSet();
        if (Objects.nonNull(services)) {
            services.forEach(serviceMetadata -> {
                serviceMetadata.restfulDefinitions().forEach(endpointDefinition -> {
                    final String content = new String(endpointDefinition.getContent(), StandardCharsets.UTF_8);
                    final Yaml yaml = new Yaml();
                    final OpenAPI openAPI = yaml.loadAs(content, OpenAPI.class);
                    final String version = openAPI.getInfo().getVersion();
                    final Paths paths = openAPI.getPaths();
                    for (Map.Entry<String, PathItem> path : paths.entrySet()) {
                        final String pathKey = path.getKey();
                        PathItem pathItem = null;
                        if (path.getValue() instanceof PathItem) {
                            pathItem = path.getValue();
                        } else {
                            final String encode = Json.encode(path.getValue());
                            pathItem = Json.decodeValue(encode, PathItem.class);
                        }
                        if (Objects.nonNull(pathItem)) {
                            final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
                            operationMap.forEach((httpMethod, operation) -> {
                                final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
                                if (Objects.isNull(endpoint)) {
                                    throw new RuntimeException("x-grpc-endpoint must defined in you proto file");
                                }
                                RestfulHttpApi restfulHttpApi = new RestfulHttpApi();
                                Set<HttpMethod> methods = Sets.newHashSet();
                                restfulHttpApi.setPath(basePath + pathKey);
                                methods.add(new HttpMethod(httpMethod.name()));
                                restfulHttpApi.setMethods(methods);
                                restfulHttpApi.setGrpcDefination(endpoint.toString());
                                restfulHttpApi.setApiResponses(operation.getResponses());
                                httpApis.add(restfulHttpApi);
                            });
                        }
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
        if (httpApi instanceof RestfulHttpApi) {
            final RestfulHttpApi restfulHttpApi = (RestfulHttpApi) httpApi;

            Optional<RestfulApi> restfulApi = restfulApiAtomicReference.get().stream().filter(api -> {
                return api.getGrpcDefination().equalsIgnoreCase(restfulHttpApi.getGrpcDefination());
            }).collect(Collectors.toList()).stream().findFirst();
            if (restfulApi.isPresent()) {
                final CompletableFuture<Object> completableFuture = restfulApi.get().execute(config.getPath(), restfulHttpApi, request);
                completableFuture.whenComplete((result, t) -> {
                    HttpServerResponse response = request.response();
                    response.headers().set(HttpHeaders.CONTENT_TYPE, RestfulConstants.CONTENT_TYPE);
                    String chunk = gson.toJson(result);
                    response.end(chunk);
                });
            }
            if (restfulApi.isEmpty()) {
                // TODO
                return;
            }
        } else {
            // 抛出异常
        }
    }
}
