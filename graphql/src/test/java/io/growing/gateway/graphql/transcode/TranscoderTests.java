package io.growing.gateway.graphql.transcode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.protobuf.ByteString;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class TranscoderTests {

    @Test
    public void test() {
        final Map<String, Object> attrs = ImmutableMap.of("type", "json", "name", "name");
        final Map<String, Object> source = ImmutableMap.of("id", 1, "attrs", attrs);
        final List<String> mappings = Lists.newArrayList("id=ids.add", "attrs=...", "attrs=params.any:/AnyDto");
        final List<String> values = Lists.newArrayList("stage=NONE");
        final Transcoder transcoder = new Transcoder() {
        };
        final Map<String, Object> target = transcoder.transcode(source, values, mappings);
        System.out.println(target);

        ByteString.copyFromUtf8("eyJtZXRyaWNUeXBlIjoibm9uZSIsInN1YkNoYXJ0VHlwZSI6InNlcGVyYXRlIn0=").toByteArray();
        //assertions
    }

}
