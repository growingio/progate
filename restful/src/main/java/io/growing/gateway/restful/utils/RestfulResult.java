package io.growing.gateway.restful.utils;

import java.io.Serializable;

/***
 * @date: 2021/9/30 3:16 下午
 * @description: restful 响应结果处理
 * @author: zhuhongbin
 **/
public class RestfulResult implements Serializable {
    private String code;
    private String error;
    private Integer elasped;
    private Object data;

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

    public Integer getElasped() {
        return elasped;
    }

    public void setElasped(Integer elasped) {
        this.elasped = elasped;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}