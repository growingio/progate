package io.growing.gateway.example.error;

/**
 * @Description: 描述
 * @Author: zhuhongbin
 * @Date 2021/11/30 3:20 下午
 **/
public class CommonError extends Throwable {
    private String code;
    private String message;

    public CommonError() {
    }

    public CommonError(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
