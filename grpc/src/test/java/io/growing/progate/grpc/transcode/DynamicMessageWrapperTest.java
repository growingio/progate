package io.growing.progate.grpc.transcode;

import com.google.common.collect.Sets;
import com.google.protobuf.Any;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import io.growing.gateway.AnyValueDto;
import io.growing.gateway.MetadataDto;
import io.growing.gateway.UpstreamDto;
import io.growing.gateway.grpc.transcode.DynamicMessageWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

class DynamicMessageWrapperTest {

    @Test
    void test() throws InvalidProtocolBufferException {
        final String desc = "hello demo";
        final String name = "demo";
        final AnyValueDto value = AnyValueDto.newBuilder().setName("type").setValue("json").build();
        final UpstreamDto upstreamDto = UpstreamDto.newBuilder()
            .setName(name).setDescription(StringValue.of(desc))
            .addAllTags(Sets.newHashSet("new", "gateway"))
            .setMetadata(MetadataDto.newBuilder().setValue("meta").build()).addValues(Any.pack(value)).setVal(Any.pack(value)).build();
        final List<Descriptors.Descriptor> descriptors = List.of(AnyValueDto.getDescriptor());
        final DynamicMessage dynamicMessage = DynamicMessage.parseFrom(UpstreamDto.getDescriptor(), upstreamDto.toByteArray());
        final DynamicMessageWrapper wrapper = new DynamicMessageWrapper(dynamicMessage, descriptors);
        Assertions.assertEquals(name, wrapper.get("name"));
        Assertions.assertEquals(desc, wrapper.get("description"));
        Assertions.assertNull(wrapper.get("age"));
        final List<Object> values = (List<Object>) wrapper.get("values");
        Assertions.assertEquals(1, values.size());
        final Map<String, Object> any = (Map<String, Object>) values.get(0);
        Assertions.assertEquals("type", any.get("name"));
        Assertions.assertEquals("json", any.get("value"));
    }

    @Test
    void testAny() throws InvalidProtocolBufferException {
        final List<Descriptors.Descriptor> descriptors = List.of(AnyValueDto.getDescriptor());
        final AnyValueDto value = AnyValueDto.newBuilder().setName("type").setValue("json").build();
        final Any any = Any.pack(value);
        final DynamicMessage dynamicMessage = DynamicMessage.parseFrom(Any.getDescriptor(), any.toByteArray());
        final DynamicMessageWrapper wrapper = new DynamicMessageWrapper(dynamicMessage, descriptors);
        Assertions.assertEquals("type", wrapper.get("name"));
        Assertions.assertEquals("json", wrapper.get("value"));
        Assertions.assertEquals(false, wrapper.get("isSystem"));
    }

    @Test
    void testToValue() throws InvalidProtocolBufferException {
        final String v = "happy";
        final StringValue value = StringValue.of(v);
        final DynamicMessage dm = DynamicMessage.parseFrom(StringValue.getDescriptor(), value.toByteArray());
        final Optional<Object> valueOpt = DynamicMessageWrapper.extractValue(dm);
        Assertions.assertTrue(valueOpt.isPresent());
        Assertions.assertEquals(v, valueOpt.get());
    }

    @Test
    void testDefaultValue() throws InvalidProtocolBufferException {
        Descriptors.Descriptor descriptor = BoolValue.getDescriptor();
        final DynamicMessage dm = DynamicMessage.parseFrom(descriptor, BoolValue.of(false).toByteArray());
        final Optional<Object> valueOpt = DynamicMessageWrapper.extractValue(dm);
        Assertions.assertTrue(valueOpt.isPresent());
        Assertions.assertEquals(false, valueOpt.get());
    }

}
