package io.growing.gateway.restful;

import com.google.common.collect.Sets;
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
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
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
                        Set<HttpMethod> methods = Sets.newHashSet();
                        if (Objects.nonNull(pathItem)) {
                            HttpApi httpApi = new HttpApi();
                            httpApi.setPath(basePath + pathKey);
                            final Set<PathItem.HttpMethod> httpMethods = pathItem.readOperationsMap().keySet();
                            httpMethods.forEach(httpMethod -> {
                                methods.add(new HttpMethod(httpMethod.name()));
                            });
                            httpApi.setGrpcService(pathItem.getSummary());
                            httpApi.setUpstreamName("");
                            httpApis.add(httpApi);
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
        Optional<RestfulApi> restfulApi = restfulApiAtomicReference.get().stream().filter(api -> {
            return api.getGrpcService().equalsIgnoreCase(httpApi.getGrpcService());
        }).collect(Collectors.toList()).stream().findFirst();
        if (restfulApi.isPresent()) {
            restfulApi.get().execute(config.getPath(), httpApi, request);
        }
    }
}
