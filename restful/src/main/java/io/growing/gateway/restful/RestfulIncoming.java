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
import io.growing.gateway.restful.handler.RestfulExceptionHandler;
import io.growing.gateway.restful.idl.RestfulApi;
import io.growing.gateway.restful.idl.RestfulBuilder;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/***
 * @date: 2021/9/18 5:38 下午
 * @description: restful
 * @author: zhuhongbin
 **/
public class RestfulIncoming implements Incoming {

    private final String contentType = "application/json;charset=utf-8";
    private final Logger logger = LoggerFactory.getLogger(RestfulIncoming.class);
    private final AtomicReference<RestfulApi> restfulApiAtomicReference = new AtomicReference<>();

    private final RestfulExceptionHandler restfulExceptionHandler = new RestfulExceptionHandler();
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
        // reload 加载接口的定义和映射
        logger.info("加载的service：{}");
        RestfulBuilder restfulBuilder = RestfulBuilder.newBuilder();
        restfulApiAtomicReference.set(restfulBuilder.configFactory(configFactory).outgoings(outgoings).services(services).exceptionHandler(restfulExceptionHandler).build());
    }

    @Override
    public Set<HttpApi> apis() {
        // 从配置中加载所有预定义的接口和service 的映射
        return Sets.newHashSet(config.getHttpApis());
    }

    @Override
    public void handle(HttpServerRequest request) {
        // 上下文路径
        final String contextPath = config.getContextPath();
        // accessToken
        final String accessToken = request.getParam("access_token");
        // 获取path 查找对应的service
        request.path();
        restfulApiAtomicReference.get().execute(request);
    }

}
