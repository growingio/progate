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
import io.growing.gateway.utilities.CollectionUtilities;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamicMessageWrapper extends HashMap<String, Object> {
    private static final Set<String> WELL_KNOWN_VALUE_FIELDS = Sets.newHashSet(
        createFieldName(BoolValue.getDescriptor().getFullName()),
        createFieldName(Int32Value.getDescriptor().getFullName()),
        createFieldName(UInt32Value.getDescriptor().getFullName()),
        createFieldName(Int64Value.getDescriptor().getFullName()),
        createFieldName(UInt64Value.getDescriptor().getFullName()),
        createFieldName(StringValue.getDescriptor().getFullName()),
        createFieldName(BytesValue.getDescriptor().getFullName()),
        createFieldName(FloatValue.getDescriptor().getFullName()),
        createFieldName(DoubleValue.getDescriptor().getFullName())
    );
    private static final String WELL_KNOWN_ANY = createFieldName(Any.getDescriptor().getFullName());

    private static String createFieldName(final String fullTypeName) {
        return fullTypeName + ".value";
    }

    private final Map<String, Object> values;
    private final Set<Descriptors.Descriptor> descriptors;

    public DynamicMessageWrapper(DynamicMessage origin, Set<Descriptors.Descriptor> descriptors) {
        this.values = new HashMap<>();
        try {
            final TypeRegistry.Builder builder = TypeRegistry.newBuilder();
            descriptors.forEach(builder::add);
            final String json = JsonFormat.printer().usingTypeRegistry(builder.build()).print(origin);
            Map map = new Gson().fromJson(json, Map.class);
            values.putAll(map);
        } catch (InvalidProtocolBufferException e) {

            e.printStackTrace();
        }

//        final Optional<DynamicMessage> anyOpt = extractAny(origin, descriptors);
//        DynamicMessage message = origin;
//        if (anyOpt.isPresent()) {
//            message = anyOpt.get();
//        }
//        this.values = new HashMap<>();
//        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
//            values.put(entry.getKey().getName(), entry.getValue());
//            if (!entry.getKey().getName().equals(entry.getKey().getJsonName())) {
//                values.put(entry.getKey().getJsonName(), entry.getValue());
//            }
//        }
//        message.getDescriptorForType().getFields().forEach(field -> {
//            if (field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) {
//                final Object defaultValue = field.getDefaultValue();
//                if (!values.containsKey(field.getName())) {
//                    values.put(field.getName(), defaultValue);
//                }
//                if (!values.containsKey(field.getJsonName())) {
//                    values.put(field.getJsonName(), defaultValue);
//                }
//            }
//        });
        this.descriptors = descriptors;
    }

    @Override
    public Object get(Object key) {
        if (containsKey(key)) {
            final Object value = values.get(key);
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

    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

    private Object wrapObject(final Object value) {
        if (value instanceof DynamicMessage) {
            final DynamicMessage field = (DynamicMessage) value;
            if (field.getAllFields().size() == 1) {
                for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : field.getAllFields().entrySet()) {
                    if (WELL_KNOWN_VALUE_FIELDS.contains(entry.getKey().getFullName())) {
                        return entry.getValue();
                    }
                }
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

    private Optional<DynamicMessage> extractAny(final DynamicMessage field, final Set<Descriptors.Descriptor> descriptors) {
        if (field.getAllFields().size() == 2) {
            for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : field.getAllFields().entrySet()) {
                if (WELL_KNOWN_ANY.equals(entry.getKey().getFullName()) && CollectionUtilities.isNotEmpty(descriptors)) {
                    final DynamicMessageWrapper any = new DynamicMessageWrapper(field, null);
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
