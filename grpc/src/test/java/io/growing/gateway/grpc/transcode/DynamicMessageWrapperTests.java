package io.growing.gateway.grpc.transcode;

import com.google.common.collect.Sets;
import com.google.protobuf.Any;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import io.growing.gateway.AnyValueDto;
import io.growing.gateway.MetadataDto;
import io.growing.gateway.UpstreamDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DynamicMessageWrapperTests {

    @Test
    public void test() throws InvalidProtocolBufferException {
        final String desc = "hello demo";
        final String name = "demo";
        final AnyValueDto value = AnyValueDto.newBuilder().setName("type").setValue("json").build();
        final UpstreamDto upstreamDto = UpstreamDto.newBuilder()
            .setName(name).setDescription(StringValue.of(desc))
            .addAllTags(Sets.newHashSet("new", "gateway"))
            .setMetadata(MetadataDto.newBuilder().setValue("meta").build())
            .setValue(Any.pack(value)).build();
        final DynamicMessage dynamicMessage = DynamicMessage.parseFrom(UpstreamDto.getDescriptor(), upstreamDto.toByteArray());
        final DynamicMessageWrapper wrapper = new DynamicMessageWrapper(dynamicMessage);
        Assertions.assertEquals(name, wrapper.get("name"));
        Assertions.assertEquals(desc, wrapper.get("description"));
        Assertions.assertNull(wrapper.get("age"));
        System.out.println(wrapper.get("value"));
        Assertions.assertEquals("meta", ((Map<String, Object>) wrapper.get("metadata")).get("value"));
    }

    @Test
    public void testSer() {

    }

}
