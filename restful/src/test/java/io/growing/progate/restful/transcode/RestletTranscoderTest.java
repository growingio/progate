package io.growing.progate.restful.transcode;

import io.growing.progate.resource.ClassPathResource;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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

    @Test
    void testParseBody() throws IOException {
        final RestletTranscoder transcoder = new RestletTranscoder(null);
        final MediaType mediaType = buildMediaType();
        final ClassPathResource resource = new ClassPathResource("/body.json");
        final JsonObject json = new JsonObject(resource.utf8String());
        final Map<String, Object> body = transcoder.parseBody(json, mediaType);
        Assertions.assertNotNull(body);
        Assertions.assertEquals(5, body.size());
        Set.of("name", "id", "device", "properties", "tags").forEach(key -> Assertions.assertTrue(body.containsKey(key)));
        final Object properties = body.get("properties");
        Assertions.assertTrue(properties instanceof List);
        for (Object element : (List) properties) {
            Assertions.assertTrue(element instanceof Map);
        }
    }


    private MediaType buildMediaType() {
        final MediaType mediaType = new MediaType();
        final ObjectSchema schema = new ObjectSchema();
        schema.addProperties("id", new IntegerSchema());
        schema.addProperties("name", new StringSchema());
        schema.addProperties("tags", new ArraySchema().type("string"));
        final ObjectSchema properties = new ObjectSchema();
        properties.addProperties("key", new StringSchema());
        properties.addProperties("value", new StringSchema());
        schema.addProperties("properties", new ArraySchema().items(properties));
        final ObjectSchema device = new ObjectSchema();
        device.addProperties("id", new StringSchema());
        schema.addProperties("device", device);
        mediaType.schema(schema);
        return mediaType;
    }

}
