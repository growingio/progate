package io.growing.gateway.progate.transcode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import io.growing.gateway.graphql.transcode.Transcoder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class TranscoderTest {

    @Test
    void test() {
        final Map<String, Object> attrs = ImmutableMap.of("type", "json", "name", "name");
        final Map<String, Object> source = new HashMap<>();
        source.put("id", 1);
        source.put("attrs", attrs);
        final List<String> mappings = Lists.newArrayList("id=ids.add", "attrs=...", "attrs=params.any:/AnyDto");
        final List<String> values = Lists.newArrayList("stage=NONE");
        final Transcoder transcoder = new Transcoder() {
        };
        final Map<String, Object> target = transcoder.transcode(source, values, mappings);

        ByteString.copyFromUtf8("eyJtZXRyaWNUeXBlIjoibm9uZSIsInN1YkNoYXJ0VHlwZSI6InNlcGVyYXRlIn0=").toByteArray();
        //assertions

        Assertions.assertEquals("name", target.get("name"));
        Assertions.assertEquals("json", target.get("type"));
        Assertions.assertEquals("NONE", target.get("stage"));

        final Map<String, String> params = (Map<String, String>) target.get("params");
        Assertions.assertEquals("/AnyDto", params.get("@type"));
        final Object[] ids = (Object[]) target.get("ids");
        Assertions.assertEquals(1, ids[0]);
        Assertions.assertEquals(1, ids.length);
    }

}
