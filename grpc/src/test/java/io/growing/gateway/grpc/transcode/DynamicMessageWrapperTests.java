package io.growing.gateway.grpc.transcode;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class DynamicMessageWrapperTests {


    @Test
    public void test() throws InvalidProtocolBufferException {
//        final String desc = "hello demo";
//        final String name = "demo";
//        final AnyValueDto value = AnyValueDto.newBuilder().setName("type").setValue("json").build();
//        final UpstreamDto upstreamDto = UpstreamDto.newBuilder()
//            .setName(name).setDescription(StringValue.of(desc))
//            .addAllTags(Sets.newHashSet("new", "gateway"))
//            .setMetadata(MetadataDto.newBuilder().setValue("meta").build()).addValues(Any.pack(value)).build();
//        final Set<Descriptors.Descriptor> descriptors = Sets.newHashSet(AnyValueDto.getDescriptor());
//        final DynamicMessage dynamicMessage = DynamicMessage.parseFrom(UpstreamDto.getDescriptor(), upstreamDto.toByteArray());
//        final DynamicMessageWrapper wrapper = new DynamicMessageWrapper(dynamicMessage, descriptors);
//        Assertions.assertEquals(name, wrapper.get("name"));
//        Assertions.assertEquals(desc, wrapper.get("description"));
//        Assertions.assertNull(wrapper.get("age"));
//        final List<DynamicMessageWrapper> values = (List<DynamicMessageWrapper>) wrapper.get("values");
//        Assertions.assertEquals(1, values.size());
//        final DynamicMessageWrapper any = values.get(0);
//        Assertions.assertEquals("type", any.get("name"));
//        Assertions.assertEquals("json", any.get("value"));
//        Assertions.assertEquals("meta", ((Map<String, Object>) wrapper.get("metadata")).get("value"));
    }

    @Test
    public void testAny() throws InvalidProtocolBufferException {
//        final Set<Descriptors.Descriptor> descriptors = Sets.newHashSet(AnyValueDto.getDescriptor());
//        final AnyValueDto value = AnyValueDto.newBuilder().setName("type").setValue("json").build();
//        final Any any = Any.pack(value);
//        final DynamicMessage dynamicMessage = DynamicMessage.parseFrom(Any.getDescriptor(), any.toByteArray());
//        final DynamicMessageWrapper wrapper = new DynamicMessageWrapper(dynamicMessage, descriptors);
//        Assertions.assertEquals("type", wrapper.get("name"));
//        Assertions.assertEquals("json", wrapper.get("value"));
//        Assertions.assertEquals(false, wrapper.get("isSystem"));
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
}
