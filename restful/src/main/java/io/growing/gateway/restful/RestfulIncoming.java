package io.growing.gateway.restful;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.growing.gateway.config.ConfigFactory;
import io.growing.gateway.http.HttpApi;
import io.growing.gateway.meta.ServiceMetadata;
import io.growing.gateway.pipeline.Incoming;
import io.growing.gateway.pipeline.Outgoing;
import io.growing.gateway.restful.config.RestfulConfig;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;

/***
 * @date: 2021/9/18 5:38 下午
 * @description: restful
 * @author: zhuhongbin
 **/
public class RestfulIncoming implements Incoming {

    private final String contentType = "application/json;charset=utf-8";
    private final Logger logger = LoggerFactory.getLogger(RestfulIncoming.class);
    private final Gson gson;
    private final RestfulConfig config;
    private final ConfigFactory configFactory;

    public RestfulIncoming(RestfulConfig config, ConfigFactory configFactory) {
        this.config = config;
        this.configFactory = configFactory;
        this.gson = new GsonBuilder().serializeNulls().create();
    }

    @Override
    public void reload(final List<ServiceMetadata> services, final Set<Outgoing> outgoings) {
    }

    @Override
    public Set<HttpApi> apis() {
        final HttpApi httpApi = new HttpApi();
        String path = "/api";
        if (StringUtils.isNoneBlank(config.getPath())) {
            path = config.getPath();
        }
        httpApi.setPath(path);
        httpApi.setMethods(Sets.newHashSet(HttpMethod.POST));
        return Sets.newHashSet(httpApi);
    }

    @Override
    public void handle(HttpServerRequest request) {

    }

}
