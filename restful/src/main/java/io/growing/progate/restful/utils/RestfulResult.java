package io.growing.progate.restful.utils;

import io.growing.progate.restful.enums.ResultCode;

import java.io.Serializable;

/***
 * @date: 2021/9/30 3:16 下午
 * @description: restful 响应结果处理
 * @author: zhuhongbin
 **/
public class RestfulResult<T> implements Serializable {
    private String code;
    private String error;
    private Long elasped;
    private Object data;

    public RestfulResult() {
    }

    public RestfulResult(String code, T data, Long elasped) {
        this.code = code;
        this.data = data;
        this.elasped = elasped;
    }

    public RestfulResult(String code, String error) {
        this.code = code;
        this.error = error;
    }

    public RestfulResult(String code, T data) {
        this.code = code;
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Long getElasped() {
        return elasped;
    }

    public void setElasped(Long elasped) {
        this.elasped = elasped;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static <T> RestfulResult<T> success(T data) {
        return new RestfulResult<T>(ResultCode.SUCCESS.code(), data);
    }

    /**
     * Success.
     *
     * @param <T>  the generic type
     * @param data the data
     * @return the result vo
     */
    public static <T> RestfulResult<T> success(T data, Long elasped) {
        return new RestfulResult<T>(ResultCode.SUCCESS.code(), data, elasped);
    }

    /**
     * Error.
     *
     * @param <T> the generic type
     * @return the result vo
     */
    public static <T> RestfulResult<T> error(String error) {
        return new RestfulResult<T>(ResultCode.ERROR.code(), error);
    }
}