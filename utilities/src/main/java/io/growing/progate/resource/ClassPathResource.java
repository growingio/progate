package io.growing.progate.resource;

import com.google.common.io.ByteStreams;
import io.growing.progate.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class ClassPathResource implements Resources.Resource {

    private final String path;

    public ClassPathResource(String path) {
        this.path = path;
    }

    @Override
    public byte[] bytes() throws IOException {
        try (final InputStream is = this.getClass().getResourceAsStream(path)) {
            if (Objects.isNull(is)) {
                return new byte[]{};
            }
            return ByteStreams.toByteArray(is);
        }
    }

    @Override
    public String utf8String() throws IOException {
        return new String(bytes(), StandardCharsets.UTF_8);
    }

}
