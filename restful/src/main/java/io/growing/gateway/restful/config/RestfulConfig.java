package io.growing.gateway.restful.config;

import io.growing.gateway.http.HttpApi;

import java.util.Set;

/**
 * @Description: Rest 配置定义
 * @Author: zhuhongbin
 * @Date 2021/9/18 5:41 下午
 **/
public class RestfulConfig {
    private String contextPath;
    private Set<HttpApi> httpApis;

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Set<HttpApi> getHttpApis() {
        return httpApis;
    }

    public void setHttpApis(Set<HttpApi> httpApis) {
        this.httpApis = httpApis;
    }
}
