package io.growing.progate.restful.transcode;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RestletTranscoderTest {

    @Test
    void testSerialize() {
        final List<Map<String, Object>> result = new ArrayList<>();
        result.add(Map.of("id", 1, "name", "Cat", "tags", new String[]{"yang"}));
        result.add(Map.of("id", 2, "name", "Tiger", "tags", new String[]{"yellow"}));

        final MediaType mediaType = new MediaType();
        final ArraySchema schema = new ArraySchema();
        final ObjectSchema items = new ObjectSchema();
        items.addProperties("id", new IntegerSchema());
        items.addProperties("name", new StringSchema());
        items.addProperties("tags", new ArraySchema().type("string"));
        schema.setItems(items);
        mediaType.schema(schema);

        final RestletTranscoder transcoder = new RestletTranscoder();
        final Object obj = transcoder.serialize(result, mediaType);
        Assertions.assertTrue(obj instanceof List);
        Assertions.assertEquals("[{\"id\":1,\"name\":\"Cat\",\"tags\":[\"yang\"]},{\"id\":2,\"name\":\"Tiger\",\"tags\":[\"yellow\"]}]", Json.encode(obj));
    }

}
