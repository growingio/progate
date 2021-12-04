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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
            final Map<String, Object> extensions = parameter.getExtensions();
            final String name = parameter.getName();
            final Optional<Coercing> coercingOpt = getCoercing(extensions);
            if (parameter.getSchema() instanceof ArraySchema) {
                //only support pass to query string
                final List<Object> values = new LinkedList<>();
                request.params().entries().forEach(entry -> {
                    if ((name + "[]").equals(entry.getKey())) {
                        coercingOpt.ifPresentOrElse(coercing -> values.add(coercing.parseValue(entry.getValue())), () -> values.add(entry.getValue()));
                    }
                });
                args.put(name, values);
            } else {
                final String from = getParameterFrom(extensions).orElse(name);
                final String value = "header".equalsIgnoreCase(parameter.getIn()) ? request.getHeader(from) : request.getParam(from);
                if (Objects.isNull(value)) {
                    continue;
                }
                coercingOpt.ifPresentOrElse(coercing -> args.put(name, coercing.parseValue(value)), () -> args.put(name, value));
            }
        }
        return args;
    }

    public Map<String, Object> parseBody(final JsonObject body, final MediaType mediaType) {
        if (Objects.isNull(mediaType)) {
            return Collections.emptyMap();
        }
        final Map<String, Object> arguments = new LinkedHashMap<>();
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


    @SuppressWarnings("unchecked")
    private void extractBody(final JsonObject body, final Map<String, Object> arguments, final Schema schema) {
        final Map<String, Schema> properties = getSchemaProperties(schema);
        properties.forEach((name, s) -> {
            final Object value = body.getValue(name);
            if (Objects.isNull(value)) {
                return;
            }
            final Optional<Coercing> coercingOpt = getCoercing(s);
            if (s instanceof ArraySchema) {
                final JsonArray array = body.getJsonArray(name);
                final List<Object> entries = new ArrayList<>(array.size());
                for (Object element : array) {
                    if (element instanceof JsonObject) {
                        final Map<String, Object> elementObject = new LinkedHashMap<>();
                        extractBody((JsonObject) element, elementObject, ((ArraySchema) s).getItems());
                        entries.add(elementObject);
                    } else {
                        coercingOpt.ifPresentOrElse(
                            coercing -> entries.add(coercing.parseValue(element)),
                            () -> entries.add(element));
                    }
                }
                arguments.put(name, entries);
            } else {
                final Map<String, Schema> subProperties = getSchemaProperties(s);
                if (Objects.isNull(subProperties) || subProperties.isEmpty()) {
                    coercingOpt.ifPresentOrElse(
                        coercing -> arguments.put(name, coercing.parseValue(body.getValue(name))),
                        () -> arguments.put(name, body.getValue(name)));
                } else {
                    final JsonObject subObject = body.getJsonObject(name);
                    if (Objects.nonNull(subObject)) {
                        final Map<String, Object> elementObject = new LinkedHashMap<>();
                        extractBody(subObject, elementObject, s);
                        arguments.put(name, elementObject);
                    }
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

    private Optional<String> getParameterFrom(final Map<String, Object> extensions) {
        if (Objects.isNull(extensions) || extensions.isEmpty()) {
            return Optional.empty();
        }
        final String from = (String) extensions.get("x-from");
        if (Objects.isNull(from) || from.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(from);
    }


}
