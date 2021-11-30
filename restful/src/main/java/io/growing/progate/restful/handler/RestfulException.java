package io.growing.progate.restful.handler;

/**
 * @Description: RestfulException
 * @Author: zhuhongbin
 * @Date 2021/10/28 1:58 下午
 **/
public class RestfulException extends RuntimeException {
    public RestfulException(String message) {
        super(message);
    }
}
