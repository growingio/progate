package io.growing.gateway.restful.utils;

/**
 * @Description: restful 常量的定义
 * @Author: zhuhongbin
 * @Date 2021/9/28 2:30 下午
 **/
public class RestfulConstants {

    /****
     * grpc endpoint 定义
     **/
    public static final String X_GRPC_ENDPOINT = "x-grpc-endpoint";
    /***
     * CONTENT_TYPE 请求或者响应mideatype
     **/
    public static final String CONTENT_TYPE = "application/json;charset=utf-8";

    /****
     * rest 请求的path 变量
     **/
    public static final String REST_PATH_KEY = "{projectId}";
    /****
     * vertx 请求的path 变量
     **/
    public static final String VERTX_PATH_KEY = ":projectId";
    /****
     * PROJECT_KEY
     **/
    public static final String PROJECT_KEY = "projectId";

    public static final String OPENAPI_MEDIA_TYPE = "application/json";

    public static final String RESULT_DATA = "data";
    public static final String X_REQUEST_ID = "x-request-id";
    public static final String CLIENT_ID = "client_id";
    public static final String TOKEN = "token";
    public static final String AUTHORIZE = "Authorization";


}
