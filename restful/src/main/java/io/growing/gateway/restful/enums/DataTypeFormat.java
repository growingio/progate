package io.growing.gateway.restful.enums;

/**
 * @Description: 仅用于扩展自定义或者自己指定的类型
 * @Author: zhuhongbin
 * @Date 2021/10/28 4:55 下午
 **/
public enum DataTypeFormat {
    HASHID("hashid", "for hashid");

    DataTypeFormat(String name, String desc) {
        this.name = name;
        this.desc = desc;
    }

    /***
     *  数据类型名称
     **/
    private String name;
    /***
     *  数据类型描述
     **/
    private String desc;

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

}