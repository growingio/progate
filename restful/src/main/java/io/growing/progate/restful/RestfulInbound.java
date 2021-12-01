package io.growing.progate.restful;

import io.growing.gateway.context.RuntimeContext;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.HttpEndpoint;
import io.growing.gateway.pipeline.Inbound;
import io.growing.gateway.pipeline.Outbound;
import io.growing.progate.restful.config.RestfulConfig;
import io.growing.progate.restful.exception.NewScalarInstanceException;
import io.growing.progate.restful.transcode.Coercing;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import io.vertx.core.http.HttpMethod;

import javax.inject.Inject;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RestfulInbound implements Inbound {

    private static final Pattern PATH_PATTERN = Pattern.compile("\\{(\\w+)\\}");
    private final Map<String, Coercing> coercingSet;

    @Inject
    public RestfulInbound(RestfulConfig config) {
        this.coercingSet = new LinkedHashMap<>(config.getScalars().size());
        try {
            for (Map.Entry<String, String> entry : config.getScalars().entrySet()) {
                final Object instance = Class.forName(entry.getValue()).getConstructor(new Class[]{}).newInstance();
                coercingSet.put(entry.getKey(), (Coercing) instance);
            }
        } catch (Throwable t) {
            throw new NewScalarInstanceException(t.getMessage(), t);
        }
    }

    @Override
    public Set<HttpEndpoint> endpoints(List<ServiceMetadata> services, Set<Outbound> outbounds, RuntimeContext context) {
        return services.stream().flatMap(service -> buildEndpoints(service, outbounds).stream()).collect(Collectors.toSet());
    }

    private Set<HttpEndpoint> buildEndpoints(final ServiceMetadata service, final Set<Outbound> outbounds) {
        return service.restfulDefinitions().stream().flatMap(definition -> {
            final String content = new String(definition.getContent(), StandardCharsets.UTF_8);
            final SwaggerParseResult result = new OpenAPIV3Parser().readContents(content);
            final OpenAPI openapi = result.getOpenAPI();
            return openapi.getPaths().entrySet().stream()
                .flatMap(entry -> {
                    final String key = entry.getKey();
                    final String path = "/" + openapi.getInfo().getVersion() + PATH_PATTERN.matcher(key).replaceAll(":$1");
                    return toEndpoints(service, outbounds, path, entry.getValue()).stream();
                });
        }).collect(Collectors.toSet());
    }

    private Set<HttpEndpoint> toEndpoints(final ServiceMetadata service, final Set<Outbound> outbounds,
                                          final String path, final PathItem item) {
        return item.readOperationsMap().entrySet().stream().map(entry -> {
            final HttpEndpoint endpoint = new HttpEndpoint();
            endpoint.setMethods(Set.of(HttpMethod.valueOf(entry.getKey().name())));
            endpoint.setPath(path);
            endpoint.setHandler(Restlet.of(entry.getValue(), outbounds, service.upstream(), coercingSet));
            return endpoint;
        }).collect(Collectors.toSet());
    }

}
