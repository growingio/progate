package io.growing.progate.http;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.impl.headers.HeadersMultiMap;
import io.vertx.core.net.SocketAddress;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DirectiveTest {

    private final Directive directive = new Directive() {
    };

    @Test
    void testGetRequestId() {
        final HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        final HeadersMultiMap headers = new HeadersMultiMap();
        Mockito.when(request.headers()).thenReturn(headers);
        final String requestId = directive.getRequestId(request);
        Assertions.assertNotNull(requestId);
        Assertions.assertFalse(requestId.isEmpty());

        final String id = "12345";
        headers.add(HttpHeaders.X_REQUEST_ID, id);
        Assertions.assertEquals(id, directive.getRequestId(request));
    }

    @Test
    void testGetRemoteAddress() {
        final HttpServerRequest request = Mockito.mock(HttpServerRequest.class);
        final HeadersMultiMap headers = new HeadersMultiMap();
        final SocketAddress address = Mockito.mock(SocketAddress.class);
        Mockito.when(address.hostAddress()).thenReturn("host");
        Mockito.when(request.headers()).thenReturn(headers);
        Mockito.when(request.remoteAddress()).thenReturn(address);

        Assertions.assertEquals("host", directive.getRemoteAddress(request));

        final String realIp = "real-ip";
        headers.add("X-Real-IP", realIp);
        Assertions.assertEquals(realIp, directive.getRemoteAddress(request));

        final String forwardedFor = "x-forward-for";
        headers.add(HttpHeaders.X_FORWARDED_FOR, forwardedFor);
        Assertions.assertEquals(forwardedFor, directive.getRemoteAddress(request));
    }

    @Test
    void testAsJsonContentType() {
        final HttpServerResponse response = Mockito.mock(HttpServerResponse.class);
        Mockito.when(response.headers()).thenReturn(new HeadersMultiMap());

        directive.asJsonContentType(response);

        Assertions.assertEquals(MediaType.JSON_UTF_8.toString(), response.headers().get(HttpHeaders.CONTENT_TYPE));
    }
}
