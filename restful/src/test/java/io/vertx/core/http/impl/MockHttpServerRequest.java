package io.vertx.core.http.impl;

public class MockHttpServerRequest extends Http1xServerRequest {
    private final String uri;

    public MockHttpServerRequest(String uri) {
        super(null, null, null);
        this.uri = uri;
    }

    @Override
    public String uri() {
        return this.uri;
    }

}
