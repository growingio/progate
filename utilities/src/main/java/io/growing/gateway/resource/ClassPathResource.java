package io.growing.gateway.resource;

import com.google.common.io.ByteStreams;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ClassPathResource {

    private final String path;

    public ClassPathResource(String path) {
        this.path = path;
    }

    public byte[] bytes() throws IOException {
        try (final InputStream is = this.getClass().getResourceAsStream(path)) {
            if (Objects.isNull(is)) {
                return null;
            }
            return ByteStreams.toByteArray(is);
        }
    }

}
