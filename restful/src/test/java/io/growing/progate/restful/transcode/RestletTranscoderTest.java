package io.growing.progate.restful.transcode;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

class RestletTranscoderTest {

    @Test
    void testSerialize() {
        final List<Map<String, Object>> result = new ArrayList<>();
        final Map<String, Object> cat = Map.of("id", 1, "name", "Cat", "tags", new String[]{"yang"},
            "properties", List.of(Map.of("key", "city", "value", "Shanghai")), "device", Map.of("id", "12345"));
        result.add(cat);
        final Map<String, Object> tiger = Map.of("id", 2, "name", "Tiger", "tags", Set.of("yellow"),
            "properties", List.of(Map.of("key", "city", "value", "Beijing")));
        result.add(tiger);

        final MediaType mediaType = new MediaType();
        final ArraySchema schema = new ArraySchema();
        final ObjectSchema items = new ObjectSchema();
        items.addProperties("id", new IntegerSchema());
        items.addProperties("name", new StringSchema());
        items.addProperties("tags", new ArraySchema().type("string"));
        final ObjectSchema properties = new ObjectSchema();
        properties.addProperties("key", new StringSchema());
        properties.addProperties("value", new StringSchema());
        items.addProperties("properties", new ArraySchema().items(properties));
        final ObjectSchema device = new ObjectSchema();
        device.addProperties("id", new StringSchema());
        items.addProperties("device", device);
        schema.setItems(items);
        mediaType.schema(schema);

        final RestletTranscoder transcoder = new RestletTranscoder(null);
        final Object obj = transcoder.serialize(result, mediaType);
        Assertions.assertTrue(obj instanceof JsonArray);
        Assertions.assertTrue(transcoder.serialize(new Object[]{cat, tiger}, mediaType) instanceof JsonArray);
        System.out.println(Json.encode(obj));
        Assertions.assertEquals("[{\"id\":1,\"name\":\"Cat\",\"tags\":[\"yang\"]," +
            "\"properties\":[{\"key\":\"city\",\"value\":\"Shanghai\"}],\"device\":{\"id\":\"12345\"}}," +
            "{\"id\":2,\"name\":\"Tiger\",\"tags\":[\"yellow\"],\"properties\":[{\"key\":\"city\",\"value\":\"Beijing\"}]}]", Json.encode(obj));
    }

}
