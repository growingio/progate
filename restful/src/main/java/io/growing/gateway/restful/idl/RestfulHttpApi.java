package io.growing.gateway.restful.idl;

import io.growing.gateway.http.HttpApi;
import io.swagger.v3.oas.models.responses.ApiResponses;

/**
 * @Description: restful 定义扩展
 * @Author: zhuhongbin
 * @Date 2021/9/29 11:32 上午
 **/
public class RestfulHttpApi extends HttpApi {
    private String grpcDefination;
    private ApiResponses apiResponses;

    public String getGrpcDefination() {
        return grpcDefination;
    }

    public void setGrpcDefination(String grpcDefination) {
        this.grpcDefination = grpcDefination;
    }

    public ApiResponses getApiResponses() {
        return apiResponses;
    }

    public void setApiResponses(ApiResponses apiResponses) {
        this.apiResponses = apiResponses;
    }
}
