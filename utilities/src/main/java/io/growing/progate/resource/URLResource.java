package io.growing.progate.resource;

import io.growing.progate.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class URLResource implements Resources.Resource {

    private final String url;

    public URLResource(String url) {
        this.url = url;
    }

    @Override
    public String uri() {
        return url;
    }

    @Override
    public byte[] bytes() throws IOException {
        return com.google.common.io.Resources.toByteArray(new URL(url));
    }

    @Override
    public String utf8String() throws IOException {
        return com.google.common.io.Resources.toString(new URL(url), StandardCharsets.UTF_8);
    }

}
