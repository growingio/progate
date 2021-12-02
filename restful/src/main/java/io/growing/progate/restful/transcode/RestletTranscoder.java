package io.growing.progate.restful.transcode;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;


public class RestletTranscoder {

    private final Components components;
    private final Map<String, Coercing> coercingSet;

    public RestletTranscoder(Components components) {
        this(components, Collections.emptyMap());
    }

    public RestletTranscoder(Components components, Map<String, Coercing> coercingSet) {
        this.components = components;
        this.coercingSet = coercingSet;
    }

    public Map<String, Object> parseParameters(final HttpServerRequest request, final List<Parameter> parameters) {
        if (Objects.isNull(parameters) || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<String, Object> args = new HashMap<>(parameters.size());
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            final String value = "header".equalsIgnoreCase(parameter.getIn()) ? request.getHeader(name) : request.getParam(name);
            if (Objects.isNull(value)) {
                continue;
            }
            final Map<String, Object> extensions = parameter.getExtensions();
            getCoercing(extensions).ifPresentOrElse(coercing -> args.put(name, coercing.parseValue(value)), () -> args.put(name, value));
        }
        return args;
    }

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
            final JsonArray elements = new JsonArray();
            if (result instanceof Object[]) {
                final Object[] entries = (Object[]) result;
                for (Object value : entries) {
                    elements.add(toJsonObject(value, ((ArraySchema) schema).getItems()));
                }
            } else {
                for (Object value : (Collection) result) {
                    elements.add(toJsonObject(value, ((ArraySchema) schema).getItems()));
                }
            }
            return elements;
        }
        return toJsonObject(result, schema);
    }


    private void extractBody(final JsonObject body, final Map<String, Object> arguments, final Schema schema) {
        final Map<String, Schema> properties = getSchemaProperties(schema);
        properties.forEach((name, s) -> {
            final Map<String, Schema> subProperties = getSchemaProperties(s);
            if (Objects.isNull(subProperties) || subProperties.isEmpty()) {
                getCoercing(s).ifPresentOrElse(
                    coercing -> arguments.put(name, coercing.parseValue(body.getValue(name))),
                    () -> arguments.put(name, body.getValue(name)));
            } else {
                final JsonObject subObject = body.getJsonObject(name);
                if (Objects.nonNull(subObject)) {
                    extractBody(subObject, arguments, s);
                }
            }
        });
    }

    private JsonObject toJsonObject(final Object result, final Schema schema) {
        final JsonObject json = new JsonObject();
        final Map<String, Object> values = (Map<String, Object>) result;
        final Map<String, Schema> properties = getSchemaProperties(schema);
        properties.forEach((name, s) -> {
            final Object value = values.get(name);
            if (Objects.isNull(value)) {
                return;
            }
            if (s instanceof ArraySchema) {
                final JsonArray array = new JsonArray();
                if (value instanceof Object[]) {
                    final Object[] entries = (Object[]) value;
                    for (Object entry : entries) {
                        processArraySchema(entry, array, s);
                    }
                } else {
                    for (Object entry : (Collection) value) {
                        processArraySchema(entry, array, s);
                    }
                }
                json.put(name, array);
            } else {
                final Map<String, Schema> subProperties = getSchemaProperties(s);
                if (Objects.isNull(subProperties) || subProperties.isEmpty()) {
                    getCoercing(s).ifPresentOrElse(coercing -> json.put(name, coercing.serialize(value)), () -> json.put(name, value));
                } else {
                    json.put(name, toJsonObject(value, s));
                }
            }
        });
        return json;
    }

    private void processArraySchema(final Object entry, final JsonArray elements, final Schema elementSchema) {
        if (entry instanceof Map) {
            final Object subObject = toJsonObject(entry, ((ArraySchema) elementSchema).getItems());
            elements.add(subObject);
        } else {
            elements.add(entry);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Schema> getSchemaProperties(final Schema schema) {
        final String ref = schema.get$ref();
        if (StringUtils.isNotBlank(ref) && Objects.nonNull(components)) {
            final char flag = '/';
            final int index = ref.lastIndexOf(flag);
            final String refName = index > -1 ? ref.substring(index + 1) : ref;
            if (components.getSchemas().containsKey(refName)) {
                return components.getSchemas().get(refName).getProperties();
            }
        }
        return schema.getProperties();
    }

    private Optional<Coercing> getCoercing(final Schema schema) {
        final Map<String, Object> extensions = schema.getExtensions();
        return getCoercing(extensions);
    }

    private Optional<Coercing> getCoercing(final Map<String, Object> extensions) {
        if (coercingSet.isEmpty()) {
            return Optional.empty();
        }

        if (Objects.isNull(extensions) || extensions.isEmpty()) {
            return Optional.empty();
        }
        final String scalar = (String) extensions.get("x-scalar");
        if (Objects.isNull(scalar) || scalar.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(coercingSet.get(scalar));
    }

}
