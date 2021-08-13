package io.growing.gateway.grpc.internal;

import com.google.common.io.ByteStreams;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author AI
 */
public class ClassPathResource {
    private final Logger logger = LoggerFactory.getLogger(ClassPathResource.class);
    private final String path;

    public ClassPathResource(String path) {
        this.path = path;
    }

    public byte[] bytes() {
        try (final InputStream is = this.getClass().getResourceAsStream(path)) {
            if (Objects.isNull(is)) {
                return null;
            }
            return ByteStreams.toByteArray(is);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
