package io.growing.gateway.grpc.transcode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
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
import com.google.protobuf.StringValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public DynamicMessageWrapper(DynamicMessage message) {
        final ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : message.getAllFields().entrySet()) {
            builder.put(entry.getKey().getName(), entry.getValue());
            if (!entry.getKey().getName().equals(entry.getKey().getJsonName())) {
                builder.put(entry.getKey().getJsonName(), entry.getValue());
            }
        }
        this.values = builder.build();
    }

    @Override
    public Object get(Object key) {
        if (containsKey(key)) {
            final Object value = values.get(key);
            if (value instanceof DynamicMessage) {
                final DynamicMessage field = (DynamicMessage) value;
                if (field.getAllFields().size() == 1) {
                    for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : field.getAllFields().entrySet()) {
                        if (WELL_KNOWN_VALUE_FIELDS.contains(entry.getKey().getFullName())) {
                            return entry.getValue();
                        }
                    }
                } else if (field.getAllFields().size() == 2) {
                    for (Map.Entry<Descriptors.FieldDescriptor, Object> entry : field.getAllFields().entrySet()) {
                        if (WELL_KNOWN_ANY.equals(entry.getKey().getFullName())) {
                            final DynamicMessageWrapper any = new DynamicMessageWrapper(field);
                            return Any.newBuilder().setTypeUrl((String) any.get("type_url")).setValue((ByteString) entry.getValue()).build();
                        }
                    }
                }
                return new DynamicMessageWrapper(field);
            }
            return value;
        }
        return null;
    }

    @Override
    public boolean containsKey(Object key) {
        return values.containsKey(key);
    }

}
