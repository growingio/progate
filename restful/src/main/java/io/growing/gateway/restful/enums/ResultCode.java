package io.growing.gateway.restful.enums;

/**
 * @Description: 描述
 * @Author: zhuhongbin
 * @Date 2021/10/14 8:54 下午
 **/
public enum ResultCode {
    /***
     * 全局响应码 成功 000000
     **/
    SUCCESS("000000", "success"),
    /***
     * 全局响应码 失败 999999
     **/
    ERROR("999999", "error");


    private String code;
    private String desc;

    public String code() {
        return code;
    }

    public String desc() {
        return desc;
    }

    ResultCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
