package io.growing.gateway.restful.idl;

import com.google.common.collect.Sets;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Outbound;
import io.growing.gateway.restful.utils.RestfulConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class RestfulBuilder {
    private Set<Outbound> outbounds;
    private List<ServiceMetadata> services;

    public static RestfulBuilder newBuilder() {
        return new RestfulBuilder();
    }

    public RestfulBuilder services(final List<ServiceMetadata> services) {
        this.services = services;
        return this;
    }

    public RestfulBuilder outgoings(final Set<Outbound> outbounds) {
        this.outbounds = outbounds;
        return this;
    }

    public Set<RestfulApi> build() {
        Set<RestfulApi> restfulApis = Sets.newHashSet();
        if (Objects.nonNull(services)) {
            services.forEach(serviceMetadata -> {
                serviceMetadata.restfulDefinitions().forEach(endpointDefinition -> {
                    final OpenAPIV3Parser openAPIV3Parser = new OpenAPIV3Parser();
                    final SwaggerParseResult swaggerParseResult = openAPIV3Parser.readContents(new String(endpointDefinition.getContent(), StandardCharsets.UTF_8));
                    OpenAPI openAPI = swaggerParseResult.getOpenAPI();
                    final String version = openAPI.getInfo().getVersion();
                    final Paths paths = openAPI.getPaths();
                    for (Map.Entry<String, PathItem> path : paths.entrySet()) {
                        PathItem pathItem = path.getValue();
                        final Map<PathItem.HttpMethod, Operation> operationMap = pathItem.readOperationsMap();
                        operationMap.forEach((httpMethod, operation) -> {
                            restfulApis.addAll(bind(operation, outbounds, serviceMetadata));

                        });
                    }
                });
            });
        }
        return restfulApis;
    }

    private Set<RestfulApi> bind(Operation operation, Set<Outbound> outbounds, ServiceMetadata serviceMetadata) {
        Set<RestfulApi> restfulApis = Sets.newHashSet();
        final Object endpoint = operation.getExtensions().get(RestfulConstants.X_GRPC_ENDPOINT);
        if (Objects.nonNull(endpoint)) {
            outbounds.forEach(outgoing -> {
                restfulApis.add(new RestfulApi(serviceMetadata, endpoint.toString(), outgoing));
            });
            return restfulApis;
        }
        throw new RuntimeException("x-grpc-endpoint must defined in you proto file");
    }
}