package io.growing.progate.http;

import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.UUID;

public interface Directive {

    default String getRemoteAddress(final HttpServerRequest request) {
        final MultiMap headers = request.headers();
        String headerValue = headers.get(HttpHeaders.X_FORWARDED_FOR);
        if (Objects.isNull(headerValue)) {
            headerValue = headers.get("X-Real-IP");
        }
        if (StringUtils.isBlank(headerValue)) {
            return request.remoteAddress().hostAddress();
        }
        return StringUtils.split(headerValue, ',')[0];
    }

    default String getRequestId(final HttpServerRequest request) {
        final String headerValue = request.headers().get(HttpHeaders.X_REQUEST_ID);
        if (Objects.isNull(headerValue)) {
            return UUID.randomUUID().toString();
        }
        return headerValue;
    }

    default void asJsonContentType(final HttpServerResponse response) {
        response.headers().set(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString());
    }

}
