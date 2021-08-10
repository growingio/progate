package io.growing.gateway.graphql;

import com.google.common.collect.Sets;
import io.growing.gateway.api.IncomingHandler;
import io.growing.gateway.http.HttpApi;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;

import java.util.Optional;

/**
 * @author AI
 */
public class GraphqlIncomingHandler implements IncomingHandler {

    @Override
    public Optional<HttpApi> api() {
        final HttpApi httpApi = new HttpApi();
        httpApi.setPath("/graphql");
        httpApi.setMethods(Sets.newHashSet(HttpMethod.POST));
        return Optional.of(httpApi);
    }

    @Override
    public void handle(HttpServerRequest request) {

    }

}
