package io.growing.gateway.restful.rule.config;


import io.vertx.core.http.HttpMethod;

import java.util.Objects;

public class ApiRuleConfig {

    private String get;
    private String post;
    private String delete;
    private String put;

    private ParameterConfig parameter;

    private RequestConfig request;

    private ResponseConfig response;

    public HttpMethod getHttpMethod() {
        if (Objects.nonNull(get)) return HttpMethod.GET;
        if (Objects.nonNull(post)) return HttpMethod.POST;
        if (Objects.nonNull(delete)) return HttpMethod.DELETE;
        if (Objects.nonNull(put)) return HttpMethod.PUT;
        return HttpMethod.GET;
    }

    public String getPath() {
        if (Objects.nonNull(get)) return get;
        if (Objects.nonNull(post)) return post;
        if (Objects.nonNull(delete)) return delete;
        if (Objects.nonNull(put)) return put;
        return "";
    }

    public String getGet() {
        return get;
    }

    public String getPost() {
        return post;
    }

    public String getDelete() {
        return delete;
    }

    public String getPut() {
        return put;
    }

    public ParameterConfig getParameter() {
        return parameter;
    }

    public RequestConfig getRequest() {
        return request;
    }

    public ResponseConfig getResponse() {
        return response;
    }

    public void setGet(String get) {
        this.get = get;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public void setDelete(String delete) {
        this.delete = delete;
    }

    public void setPut(String put) {
        this.put = put;
    }

    public void setParameter(ParameterConfig parameter) {
        this.parameter = parameter;
    }

    public void setRequest(RequestConfig request) {
        this.request = request;
    }

    public void setResponse(ResponseConfig response) {
        this.response = response;
    }
}
