package io.growing.progate;

import io.growing.progate.resource.ClassPathResource;
import io.growing.progate.resource.URLResource;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public final class Resources {
    private Resources() {

    }

    public static Resource from(final String url) {
        final String prefix = "classpath:";
        if (url.startsWith(prefix)) {
            return new ClassPathResource(url.replace(prefix, StringUtils.EMPTY));
        }
        return new URLResource(url);
    }

    public static interface Resource {

        byte[] bytes() throws IOException;

        String utf8String() throws IOException;
    }

}
