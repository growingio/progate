package io.growing.gateway.graphql.transcode;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public interface Transcoder {

    @SuppressWarnings("unchecked")
    default Map<String, Object> transcode(final Map<String, Object> arguments, final List<String> values, final List<String> mappings) {
        values.forEach(value -> {
            final int index = value.indexOf('=');
            arguments.put(value.substring(0, index), value.substring(index + 1));
        });
        mappings.forEach(mapping -> {
            final TranscodeMapping transcode = parse(mapping);
            final Object originValue = extract(arguments, transcode.getSource());
            if (Objects.isNull(originValue)) {
                return;
            }
            if (transcode.getTarget().endsWith(".add")) {
                final Object value = new Object[]{originValue};
                set(arguments, transcode.getTarget().replace(".add", ""), value);
            } else if (transcode.getTarget().endsWith(".any")) {
                set(arguments, transcode.getTarget().replace(".any", ""), originValue);
                set(arguments, transcode.getTarget().replace(".any", ".@type"), transcode.getExtension());
            } else if (transcode.getTarget().endsWith(".bytes")) {
                set(arguments, transcode.getTarget().replace(".bytes", ""), new Gson().toJson(originValue).getBytes(StandardCharsets.UTF_8));
            } else if (transcode.getTarget().endsWith("...")) {
                arguments.putAll((Map<String, Object>) originValue);
            } else {
                set(arguments, transcode.getTarget(), originValue);
            }
        });
        return arguments;
    }


    @SuppressWarnings("unchecked")
    default Object extract(final Map<String, Object> source, final String name) {
        final char flag = '.';
        if (name.indexOf(flag) > -1) {
            final String[] names = StringUtils.split(name, flag);
            Map<String, Object> node = (Map<String, Object>) source.get(names[0]);
            for (int i = 1; i < names.length - 1; i++) {
                node = (Map<String, Object>) node.get(names[i]);
            }
            return node.get(names[names.length - 1]);
        } else {
            return source.get(name);
        }
    }

    @SuppressWarnings("unchecked")
    default void set(final Map<String, Object> target, final String name, final Object value) {
        final char flag = '.';
        if (name.indexOf(flag) > -1) {
            final String[] names = StringUtils.split(name, flag);
            Map<String, Object> node = target;
            for (int i = 0; i < names.length - 1; i++) {
                final Object nodeValue = node.get(names[i]);
                if (Objects.isNull(nodeValue)) {
                    node = new HashMap<>();
                } else {
                    node = (Map<String, Object>) nodeValue;
                }
            }
            node.put(names[names.length - 1], value);
        } else {
            put(target, name, value);
        }
    }

    default void put(final Map<String, Object> target, final String name, final Object value) {
        if (value instanceof ImmutableMap) {
            final Map<String, Object> container = new HashMap<>((Map<String, Object>) value);
            target.put(name, container);
        } else {
            target.put(name, value);
        }
    }

    default TranscodeMapping parse(final String str) {
        final int index = str.indexOf('=');
        final String source = str.substring(0, index);
        final String to = str.substring(index + 1);
        final int extensionIndex = to.indexOf(':');
        String target;
        String extension = null;
        if (extensionIndex > -1) {
            target = to.substring(0, extensionIndex);
            extension = to.substring(extensionIndex + 1);
        } else {
            target = to;
        }
        return new TranscodeMapping(source, target, extension);
    }

}
