package io.growing.gateway.restful.idl;

import com.google.common.collect.Sets;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.plugin.fetcher.PluginFetcherBuilder;
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.vertx.core.json.Json;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    /***
     * @date: 2021/9/27 5:45 下午
     * @description: 做一个全局service 和upstream 的绑定
     * @author: zhuhongbin
     **/
    public Set<RestfulApi> build() {
        // 初始化所有路由
        final String basePath = "";
        Set<RestfulApi> restfulApis = Sets.newHashSet();
        if (Objects.nonNull(services)) {
            services.forEach(serviceMetadata -> {
                serviceMetadata.restfulDefinitions().forEach(endpointDefinition -> {
                    final String content = new String(endpointDefinition.getContent(), StandardCharsets.UTF_8);
                    final Yaml yaml = new Yaml();
                    final OpenAPI openAPI = yaml.loadAs(content, OpenAPI.class);
                    final String version = openAPI.getInfo().getVersion();
                    final Paths paths = openAPI.getPaths();
                    for (Map.Entry<String, PathItem> path : paths.entrySet()) {
                        PathItem pathItem = null;
                        if (path.getValue() instanceof PathItem) {
                            pathItem = path.getValue();
                        } else {
                            final String encode = Json.encode(path.getValue());
                            pathItem = Json.decodeValue(encode, PathItem.class);
                        }
                        final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
                        operationMap.forEach((httpMethod, operation) -> {
                            restfulApis.addAll(bind(operation, outgoings, serviceMetadata));

                        });
                    }
                });
            });
        }
        return restfulApis;
    }

    private Set<RestfulApi> bind(Operation operation, Set<Outgoing> outgoings, ServiceMetadata serviceMetadata) {
        Set<RestfulApi> restfulApis = Sets.newHashSet();
        final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
        if (Objects.nonNull(endpoint)) {
            outgoings.forEach(outgoing -> {
                restfulApis.add(new RestfulApi(serviceMetadata, endpoint.toString(), outgoing, exceptionHandler, pfb));
            });
            return restfulApis;
        }
        throw new RuntimeException("x-grpc-endpoint must defined in you proto file");
    }
}