package io.growing.gateway.graphql.internal;

import com.google.common.io.CharStreams;
import io.growing.gateway.api.Upstream;
import io.growing.gateway.graphql.GraphqlSchemaScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * @author AI
 */
public class ClassPathGraphqlSchemaScanner implements GraphqlSchemaScanner {
    private final Logger logger = LoggerFactory.getLogger(ClassPathGraphqlSchemaScanner.class);

    private final String path;

    public ClassPathGraphqlSchemaScanner(String path) {
        this.path = path;
    }

    @Override
    public String scan(List<Upstream> upstreams) {
        final InputStream is = this.getClass().getResourceAsStream(path);
        if (Objects.isNull(is)) {
            return null;
        }
        try (final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return CharStreams.toString(reader);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage(), e);
            return null;
        }
    }

}
