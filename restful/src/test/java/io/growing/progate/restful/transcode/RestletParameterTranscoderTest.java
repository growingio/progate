package io.growing.progate.restful.transcode;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.vertx.core.MultiMap;
import io.vertx.core.http.impl.MockHttpServerRequest;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

class RestletParameterTranscoderTest {

    @Test
    void testParseParameter() {
        final RestletTranscoder transcoder = new RestletTranscoder(null);
        final MockHttpServerRequest request = new MockHttpServerRequest("/users/1/children/2?tags=1&tags[]=2") {
            @Override
            public String getParam(String paramName) {
                if ("id".equals(paramName)) {
                    return "1";
                } else if ("children".equals(paramName)) {
                    return "2";
                }
                return super.getParam(paramName);
            }

            @Override
            public MultiMap headers() {
                return new HeadersMultiMap().add("X-User-Id", "128");
            }
        };
        final List<Parameter> parameters = List.of(
            new Parameter().name("id").in("path"),
            new Parameter().name("tags").in("query").schema(new ArraySchema().items(new StringSchema())),
            new Parameter().name("children").in("path").schema(new ArraySchema().items(new StringSchema())),
            new Parameter().name("operator").in("header").schema(new StringSchema()).extensions(Map.of("x-from", "X-User-Id"))
        );
        final Map<String, Object> object = transcoder.parseParameters(request, parameters);
        Assertions.assertEquals(4, object.size());
        Set.of("id", "tags", "children", "operator").forEach(key -> Assertions.assertTrue(object.containsKey(key)));
        final Object tags = object.get("tags");
        Assertions.assertTrue(tags instanceof List);
        Assertions.assertEquals(2, ((List) tags).size());
        final Object children = object.get("children");
        Assertions.assertTrue(children instanceof List);
        Assertions.assertEquals(1, ((List) children).size());
    }


}
