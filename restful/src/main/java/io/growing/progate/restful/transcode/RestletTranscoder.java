package io.growing.progate.restful.transcode;

import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RestletTranscoder {

    public Map<String, Object> parseBody(final JsonObject body, final MediaType mediaType) {
        if (Objects.isNull(mediaType)) {
            return Collections.emptyMap();
        }
        final Map<String, Object> arguments = new HashMap<>();
        extractBody(body, arguments, mediaType.getSchema());
        return arguments;
    }

    public Object serialize(final Object result, final MediaType mediaType) {
        final Schema schema = mediaType.getSchema();
        if (schema instanceof ArraySchema) {
            final List<Object> elements = new LinkedList<>();
            if (result instanceof Object[]) {
                final Object[] entries = (Object[]) result;
                for (Object value : entries) {
                    elements.add(toObject(value, ((ArraySchema) schema).getItems()));
                }
            } else {
                for (Object value : (Collection) result) {
                    elements.add(toObject(value, ((ArraySchema) schema).getItems()));
                }
            }
            return elements;
        }
        return toObject(result, schema);
    }


    private void extractBody(final JsonObject body, final Map<String, Object> arguments, final Schema schema) {
        final Map<String, Schema> properties = schema.getProperties();
        properties.forEach((name, s) -> {
            final Map<String, Schema> subProperties = s.getProperties();
            if (Objects.isNull(subProperties) || subProperties.isEmpty()) {
                arguments.put(name, body.getValue(name));
            } else {
                final JsonObject subObject = body.getJsonObject(name);
                if (Objects.nonNull(subObject)) {
                    extractBody(subObject, arguments, s);
                }
            }
        });
    }

    private Object toObject(final Object result, final Schema schema) {
        final Map<String, Object> values = (Map<String, Object>) result;
        final Map<String, Object> body = new LinkedHashMap<>();
        final Map<String, Schema> properties = schema.getProperties();
        properties.forEach((name, s) -> {
            final Object value = values.get(name);
            if (s instanceof ArraySchema) {
                final JsonArray array = new JsonArray();
                if (value instanceof Object[]) {
                    final Object[] entries = (Object[]) value;
                    for (Object entry : entries) {
                        if (entry instanceof Map) {
                            final Object subObject = toObject(entry, ((ArraySchema) s).getItems());
                            array.add(subObject);
                        } else {
                            array.add(entry);
                        }
                    }
                } else {
                    for (Object entry : (Collection) value) {
                        final Object subObject = toObject(entry, ((ArraySchema) s).getItems());
                        array.add(subObject);
                    }
                }
                body.put(name, array);
            } else {
                final Map<String, Schema> subProperties = s.getProperties();
                if (Objects.isNull(subProperties) || subProperties.isEmpty()) {
                    body.put(name, value);
                } else {
                    body.put(name, toObject(value, s));
                }
            }
        });
        return body;
    }

}
