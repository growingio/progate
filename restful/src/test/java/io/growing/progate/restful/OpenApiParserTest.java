package io.growing.progate.restful;

import io.growing.gateway.resource.ClassPathResource;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class OpenApiParserTest {

    @Test
    void test() throws IOException {
        final ClassPathResource resource = new ClassPathResource("/api.yml");
        final SwaggerParseResult result = new OpenAPIV3Parser().readContents(resource.utf8String());
        final OpenAPI openapi = result.getOpenAPI();
        Assertions.assertNotNull(openapi);
        openapi.getPaths().forEach((path, item) -> {
            System.out.println(path);
            System.out.println(item);
        });
    }

}
