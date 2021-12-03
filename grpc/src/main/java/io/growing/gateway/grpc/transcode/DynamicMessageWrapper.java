package io.growing.gateway.grpc.transcode;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.BytesValue;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.FloatValue;
import com.google.protobuf.Int32Value;
import com.google.protobuf.Int64Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.google.protobuf.TypeRegistry;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;
import com.google.protobuf.util.JsonFormat;
import io.growing.gateway.grpc.helper.MapWrapper;
import io.growing.progate.utilities.CollectionUtilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicMessageWrapper extends MapWrapper<String, Object> {
    private static final Set<String> WELL_KNOWN_TYPES = Sets.newHashSet(
        BoolValue.getDescriptor().getFullName(),
        Int32Value.getDescriptor().getFullName(),
        UInt32Value.getDescriptor().getFullName(),
        Int64Value.getDescriptor().getFullName(),
        UInt64Value.getDescriptor().getFullName(),
        StringValue.getDescriptor().getFullName(),
        BytesValue.getDescriptor().getFullName(),
        FloatValue.getDescriptor().getFullName(),
        DoubleValue.getDescriptor().getFullName()
    );

    private static final Map<String, Object> WELL_KNOWN_DEFAULT_VALUES = Map.of(
        BoolValue.getDescriptor().getFullName(), false,
        Int32Value.getDescriptor().getFullName(), 0,
        UInt32Value.getDescriptor().getFullName(), 0,
        Int64Value.getDescriptor().getFullName(), 0,
        UInt64Value.getDescriptor().getFullName(), 0,
        StringValue.getDescriptor().getFullName(), "",
        BytesValue.getDescriptor().getFullName(), new byte[]{},
        FloatValue.getDescriptor().getFullName(), 0f,
        DoubleValue.getDescriptor().getFullName(), 0d
    );
    private static final String WELL_KNOWN_ANY = createFieldName(Any.getDescriptor().getFullName());

    private static String createFieldName(final String fullTypeName) {
        return fullTypeName + ".value";
    }

    private final List<Descriptors.Descriptor> descriptors;

    public static Optional<Object> extractValue(final DynamicMessage dm) {
        final String fullName = dm.getDescriptorForType().getFullName();
        if (WELL_KNOWN_TYPES.contains(fullName)) {
            return Optional.of(extractWellKnownValue(dm));
        }
        return Optional.empty();
    }

    public static Object extractWellKnownValue(final DynamicMessage dm) {
        final byte[] bytes = dm.toByteArray();
        if (bytes.length == 0) {
            return WELL_KNOWN_DEFAULT_VALUES.get(dm.getDescriptorForType().getFullName());
        }
        return dm.getAllFields().entrySet().iterator().next().getValue();
    }

    public DynamicMessageWrapper(final DynamicMessage origin, final List<Descriptors.Descriptor> descriptors) {
        super(new HashMap<String, Object>());
        Map<String, Object> underlying = super.getUnderlying();
        try {
            final TypeRegistry.Builder builder = TypeRegistry.newBuilder();
            descriptors.forEach(builder::add);
            final String json = JsonFormat.printer().includingDefaultValueFields().usingTypeRegistry(builder.build()).print(origin);
            Map map = new Gson().fromJson(json, Map.class);
            underlying.putAll(map);
            underlying.put("@type", origin.getDescriptorForType().getFullName());
        } catch (InvalidProtocolBufferException e) {
            // ignore
        }
        origin.getDescriptorForType().getFields().forEach(field -> {
            if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
                final Object value = origin.getField(field);
                if (Objects.nonNull(value)) {
                    underlying.put(field.getName(), value);
                    underlying.put(field.getJsonName(), value);
                } else {
                    final Object defaultValue = field.getDefaultValue();
                    if (!underlying.containsKey(field.getName())) {
                        underlying.put(field.getName(), defaultValue);
                    }
                    if (!underlying.containsKey(field.getJsonName())) {
                        underlying.put(field.getJsonName(), defaultValue);
                    }
                }
            } else if (field.isRepeated()) {
                final Object value = origin.getField(field);
                if (Objects.nonNull(value) && !((Collection) value).isEmpty()) {
                    underlying.put(field.getName(), value);
                    underlying.put(field.getJsonName(), value);
                }
            } else {
                final Object value = origin.getField(field);
                if (Objects.nonNull(value)) {
                    underlying.put(field.getName(), value);
                    underlying.put(field.getJsonName(), value);
                }
            }
        });
        this.descriptors = descriptors;
    }

    @Override
    public Object get(Object key) {
        if (containsKey(key)) {
            final Object value = getUnderlying().get(key);
            if (value instanceof Collection<?>) {
                final Collection<?> collection = (Collection<?>) value;
                if (CollectionUtilities.isNotEmpty(collection) && collection.iterator().next() instanceof DynamicMessage) {
                    try (final Stream<?> stream = collection.stream()) {
                        return stream.map(this::wrapObject).collect(Collectors.toList());
                    }
                }
                return collection;
            } else {
                return wrapObject(value);
            }
        }
        return null;
    }

    private Object wrapObject(final Object value) {
        if (value instanceof DynamicMessage) {
            final DynamicMessage field = (DynamicMessage) value;
            if (WELL_KNOWN_TYPES.contains(field.getDescriptorForType().getFullName())) {
                return extractWellKnownValue(field);
            } else {
                final Optional<DynamicMessage> anyOpt = extractAny(field, descriptors);
                if (anyOpt.isPresent()) {
                    return new DynamicMessageWrapper(anyOpt.get(), descriptors);
                }
            }
            return new DynamicMessageWrapper(field, descriptors);
        }
        return value;
    }

    private Optional<DynamicMessage> extractAny(final DynamicMessage field, final List<Descriptors.Descriptor> descriptors) {
        if (field.getAllFields().size() == 2) {
            for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : field.getAllFields().entrySet()) {
                if (WELL_KNOWN_ANY.equals(entry.getKey().getFullName()) && CollectionUtilities.isNotEmpty(descriptors)) {
                    final DynamicMessageWrapper any = new DynamicMessageWrapper(field, descriptors);
                    final String typeUrl = (String) any.get("type_url");
                    final int index = typeUrl.lastIndexOf('/');
                    final String fullName = typeUrl.substring(index + 1);
                    Descriptors.Descriptor typeDescriptor = null;
                    for (Descriptors.Descriptor descriptor : descriptors) {
                        if (fullName.equals(descriptor.getFullName())) {
                            typeDescriptor = descriptor;
                            break;
                        }
                    }
                    if (Objects.nonNull(typeDescriptor)) {
                        try {
                            final DynamicMessage message = DynamicMessage.parseFrom(typeDescriptor, ((ByteString) entry.getValue()));
                            return Optional.of(message);
                        } catch (InvalidProtocolBufferException e) {
                            // ignore
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }


}
